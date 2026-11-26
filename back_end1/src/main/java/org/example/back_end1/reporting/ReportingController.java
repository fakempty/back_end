package org.example.back_end1.reporting;

import org.example.back_end1.entity.Part;
import org.example.back_end1.repository.PartRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
public class ReportingController {

    private final PartRepository partRepository;
    private final ReportingCalculationService calculationService;
    private final PdfRenderService pdfRenderService;
    private final IdempotentPdfExportService idempotentPdfExportService;
    private final int maxAdminPdfRows;
    private final int csvPageSize;

    public ReportingController(
            PartRepository partRepository,
            ReportingCalculationService calculationService,
            PdfRenderService pdfRenderService,
            IdempotentPdfExportService idempotentPdfExportService,
            @Value("${lab12.reporting.max-admin-pdf-rows:100}") int maxAdminPdfRows,
            @Value("${lab12.reporting.csv-page-size:500}") int csvPageSize) {
        this.partRepository = partRepository;
        this.calculationService = calculationService;
        this.pdfRenderService = pdfRenderService;
        this.idempotentPdfExportService = idempotentPdfExportService;
        this.maxAdminPdfRows = maxAdminPdfRows;
        this.csvPageSize = Math.max(50, csvPageSize);
    }

    @GetMapping("/api/reports/parts/{id}/document.pdf")
    public ResponseEntity<byte[]> downloadPartDocumentPdf(@PathVariable("id") Long id) {
        Part part = partRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Part not found: " + id));

        PartDocumentDto docDto = calculationService.buildSinglePartDocument(part, null);
        byte[] pdfBytes = pdfRenderService.renderPdfFromTemplate("part-document.ftl", Map.of("doc", docDto));

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"part-" + id + "-order.pdf\"")
                .body(pdfBytes);
    }

    @PostMapping("/api/reports/parts/{id}/export-idempotent")
    public ResponseEntity<byte[]> exportPartPdfIdempotentPost(
            @PathVariable("id") Long id,
            @RequestParam("idempotencyKey") String idempotencyKey) {
        return executeIdempotentExport(id, idempotencyKey);
    }

    @GetMapping("/api/reports/parts/{id}/export-idempotent")
    public ResponseEntity<byte[]> exportPartPdfIdempotentGet(
            @PathVariable("id") Long id,
            @RequestParam("idempotencyKey") String idempotencyKey) {
        return executeIdempotentExport(id, idempotencyKey);
    }

    private ResponseEntity<byte[]> executeIdempotentExport(Long id, String idempotencyKey) {
        IdempotentPdfExportService.ExportResult result =
                idempotentPdfExportService.exportPartPdfIdempotently(id, idempotencyKey);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + result.fileName() + "\"")
                .header("X-Idempotent-Replayed", String.valueOf(result.replayedFromStorage()))
                .body(result.pdfBytes());
    }

    @GetMapping("/api/admin/reports/parts.pdf")
    public ResponseEntity<byte[]> downloadAdminPartsReportPdf() {
        long totalCount = partRepository.count();
        if (totalCount > maxAdminPdfRows) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "Admin PDF report limit exceeded: found " + totalCount + " items, maximum allowed is " + maxAdminPdfRows +
                            ". Use CSV export (/api/admin/reports/parts.csv) for large datasets."
            );
        }

        List<Part> parts = partRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
        PartsCatalogReportDto reportDto = calculationService.buildAdminCatalogReport(parts, maxAdminPdfRows);
        byte[] pdfBytes = pdfRenderService.renderPdfFromTemplate("parts-admin-report.ftl", Map.of("report", reportDto));

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"parts-admin-report.pdf\"")
                .body(pdfBytes);
    }

    @GetMapping("/api/admin/reports/parts.csv")
    public ResponseEntity<StreamingResponseBody> streamAdminPartsCsv() {
        StreamingResponseBody body = outputStream -> {
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8))) {
                writer.write("id,sku,name,price,quantity,created_by");
                writer.newLine();

                int page = 0;
                Page<Part> partPage;
                do {
                    partPage = partRepository.findAll(PageRequest.of(page, csvPageSize, Sort.by(Sort.Direction.ASC, "id")));
                    for (Part p : partPage.getContent()) {
                        writer.write(
                                escapeCsv(p.getId()) + "," +
                                escapeCsv(p.getSku()) + "," +
                                escapeCsv(p.getName()) + "," +
                                escapeCsv(p.getPrice()) + "," +
                                escapeCsv(p.getQuantity()) + "," +
                                escapeCsv(p.getCreatedBy())
                        );
                        writer.newLine();
                    }
                    writer.flush();
                    page++;
                } while (partPage.hasNext());
            }
        };

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"parts-export.csv\"")
                .body(body);
    }

    private String escapeCsv(Object val) {
        if (val == null) {
            return "";
        }
        String s = String.valueOf(val);
        if (s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }
}