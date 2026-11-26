package org.example.back_end1.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/batch/parts/import")
public class PartCsvImportController {

    private final SafeImportPathResolver pathResolver;
    private final JobLauncher jobLauncher;
    private final Job importPartsCsvJob;
    private final JobExplorer jobExplorer;
    private final JdbcTemplate jdbcTemplate;

    public PartCsvImportController(
            SafeImportPathResolver pathResolver,
            JobLauncher jobLauncher,
            Job importPartsCsvJob,
            JobExplorer jobExplorer,
            JdbcTemplate jdbcTemplate) {
        this.pathResolver = pathResolver;
        this.jobLauncher = jobLauncher;
        this.importPartsCsvJob = importPartsCsvJob;
        this.jobExplorer = jobExplorer;
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> startImport(
            @RequestParam("fileName") String fileName,
            @RequestParam(name = "force", defaultValue = "false") boolean force) {

        Path safeFile = pathResolver.resolveSafeFile(fileName);
        String fileHash = sha256(safeFile);

        Integer existingCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM csv_import_history WHERE file_hash = ?",
                Integer.class,
                fileHash
        );

        if (!force && existingCount != null && existingCount > 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "File '" + fileName + "' has already been imported. Pass force=true to re-import."
            );
        }

        long runId = System.currentTimeMillis();
        var params = new JobParametersBuilder()
                .addString("filePath", safeFile.toString())
                .addString("fileName", fileName)
                .addString("fileHash", fileHash)
                .addLong("run.id", runId)
                .toJobParameters();

        try {
            JobExecution execution = jobLauncher.run(importPartsCsvJob, params);
            jdbcTemplate.update(
                    "INSERT INTO csv_import_history (file_name, file_hash, status, job_execution_id, imported_by) VALUES (?, ?, ?, ?, ?)",
                    fileName,
                    fileHash,
                    execution.getStatus().name(),
                    execution.getId(),
                    PartCsvBatchJobConfig.IMPORT_IDENTITY
            );

            CompletableFuture.runAsync(() -> updateHistoryStatus(execution.getId()));

            return ResponseEntity.status(HttpStatus.ACCEPTED).body(Map.of(
                    "executionId", execution.getId(),
                    "status", execution.getStatus().name(),
                    "fileName", fileName,
                    "forced", force
            ));
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to start batch import: " + ex.getMessage(), ex);
        }
    }

    @GetMapping("/{executionId}")
    public Map<String, Object> getStatus(@PathVariable("executionId") Long executionId) {
        JobExecution execution = jobExplorer.getJobExecution(executionId);
        if (execution == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Job execution not found: " + executionId);
        }

        long readCount = 0;
        long writeCount = 0;
        long filterCount = 0;
        long skipCount = 0;

        for (StepExecution step : execution.getStepExecutions()) {
            readCount += step.getReadCount();
            writeCount += step.getWriteCount();
            filterCount += step.getFilterCount();
            skipCount += step.getSkipCount();
        }

        return Map.of(
                "executionId", execution.getId(),
                "status", execution.getStatus().name(),
                "exitCode", execution.getExitStatus().getExitCode(),
                "read", readCount,
                "write", writeCount,
                "filter", filterCount,
                "skip", skipCount
        );
    }

    private void updateHistoryStatus(Long executionId) {
        try {
            Thread.sleep(500);
            JobExecution exec = jobExplorer.getJobExecution(executionId);
            if (exec != null) {
                jdbcTemplate.update(
                        "UPDATE csv_import_history SET status = ? WHERE job_execution_id = ?",
                        exec.getStatus().name(),
                        executionId
                );
            }
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }

    private String sha256(Path path) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = Files.readAllBytes(path);
            return HexFormat.of().formatHex(digest.digest(bytes));
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Cannot compute file hash", ex);
        }
    }
}
