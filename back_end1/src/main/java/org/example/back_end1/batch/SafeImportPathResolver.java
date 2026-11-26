package org.example.back_end1.batch;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class SafeImportPathResolver {

    private final Path baseDirectory;

    public SafeImportPathResolver(@Value("${lab11.batch.input-dir:C:/lab11_imports}") String inputDir) {
        this.baseDirectory = Paths.get(inputDir).toAbsolutePath().normalize();
    }

    public Path resolveSafeFile(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "fileName must not be blank");
        }
        if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid fileName: path traversal detected");
        }

        Path resolved = baseDirectory.resolve(fileName).normalize();
        if (!resolved.startsWith(baseDirectory)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Access outside import directory is forbidden");
        }
        if (!Files.exists(resolved) || !Files.isRegularFile(resolved)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "CSV file not found: " + fileName);
        }
        return resolved;
    }
}