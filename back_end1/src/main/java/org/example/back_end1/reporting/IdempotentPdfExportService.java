package org.example.back_end1.reporting;

import jakarta.annotation.PostConstruct;
import org.example.back_end1.entity.Part;
import org.example.back_end1.repository.PartRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;

@Service
public class IdempotentPdfExportService {

    private static final Logger log = LoggerFactory.getLogger(IdempotentPdfExportService.class);

    private final PartRepository partRepository;
    private final ReportingCalculationService calculationService;
    private final PdfRenderService pdfRenderService;
    private final JdbcTemplate jdbcTemplate;
    private final Path storageDir;

    public IdempotentPdfExportService(
            PartRepository partRepository,
            ReportingCalculationService calculationService,
            PdfRenderService pdfRenderService,
            JdbcTemplate jdbcTemplate,
            @Value("${lab12.reporting.storage-dir:C:/lab12_exports}") String storageDirStr) {
        this.partRepository = partRepository;
        this.calculationService = calculationService;
        this.pdfRenderService = pdfRenderService;
        this.jdbcTemplate = jdbcTemplate;
        this.storageDir = Paths.get(storageDirStr).toAbsolutePath().normalize();
    }

    @PostConstruct
    public void initStorageAndTable() {
        try {
            Files.createDirectories(storageDir);
            jdbcTemplate.execute("""
                    CREATE TABLE IF NOT EXISTS pdf_export_records (
                        idempotency_key VARCHAR(128) PRIMARY KEY,
                        part_id BIGINT NOT NULL,
                        file_name VARCHAR(255) NOT NULL,
                        file_path VARCHAR(512) NOT NULL,
                        file_size BIGINT NOT NULL,
                        created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW()
                    )
                    """);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to initialize idempotent PDF export storage/table", ex);
        }
    }

    public record ExportResult(byte[] pdfBytes, String fileName, boolean replayedFromStorage) {}

    public ExportResult exportPartPdfIdempotently(Long partId, String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "idempotencyKey must not be blank");
        }
        String sanitizedKey = idempotencyKey.trim().replaceAll("[^a-zA-Z0-9._-]", "_");

        var existingRows = jdbcTemplate.queryForList(
                "SELECT file_name, file_path FROM pdf_export_records WHERE idempotency_key = ?",
                sanitizedKey
        );

        if (!existingRows.isEmpty()) {
            String storedFileName = String.valueOf(existingRows.get(0).get("file_name"));
            Path storedPath = Paths.get(String.valueOf(existingRows.get(0).get("file_path"))).normalize();
            if (Files.exists(storedPath) && Files.isRegularFile(storedPath)) {
                try {
                    byte[] existingBytes = Files.readAllBytes(storedPath);
                    log.info("Idempotent PDF replay for key='{}' from file='{}'", sanitizedKey, storedPath);
                    return new ExportResult(existingBytes, storedFileName, true);
                } catch (IOException ex) {
                    throw new ResponseStatusException(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            "Stored idempotent PDF exists in registry but cannot be read: " + storedPath,
                            ex
                    );
                }
            }
        }

        Part part = partRepository.findById(partId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Part not found: " + partId));

        PartDocumentDto docDto = calculationService.buildSinglePartDocument(part, sanitizedKey);
        byte[] pdfBytes = pdfRenderService.renderPdfFromTemplate("part-document.ftl", Map.of("doc", docDto));

        String fileName = "part-" + partId + "-" + sanitizedKey + ".pdf";
        Path targetFile = storageDir.resolve(fileName).normalize();
        if (!targetFile.startsWith(storageDir)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid export file path");
        }

        try {
            Files.write(targetFile, pdfBytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            jdbcTemplate.update(
                    """
                    INSERT INTO pdf_export_records (idempotency_key, part_id, file_name, file_path, file_size)
                    VALUES (?, ?, ?, ?, ?)
                    ON CONFLICT (idempotency_key) DO NOTHING
                    """,
                    sanitizedKey,
                    partId,
                    fileName,
                    targetFile.toString(),
                    (long) pdfBytes.length
            );
            log.info("Generated and persisted new idempotent PDF for key='{}' at '{}'", sanitizedKey, targetFile);
            return new ExportResult(pdfBytes, fileName, false);
        } catch (IOException ex) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to persist idempotent PDF to storage: " + ex.getMessage(),
                    ex
            );
        }
    }
}