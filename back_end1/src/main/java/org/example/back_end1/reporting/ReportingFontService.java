package org.example.back_end1.reporting;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

@Service
public class ReportingFontService {

    private static final Logger log = LoggerFactory.getLogger(ReportingFontService.class);

    private final ResourceLoader resourceLoader;
    private final String fontLocation;
    private byte[] fontBytes;

    public ReportingFontService(
            ResourceLoader resourceLoader,
            @Value("${lab12.reporting.font-classpath:classpath:fonts/DejaVuSans.ttf}") String fontLocation) {
        this.resourceLoader = resourceLoader;
        this.fontLocation = fontLocation;
    }

    @PostConstruct
    public void validateFontOnStartup() {
        Resource resource = resourceLoader.getResource(fontLocation);
        if (!resource.exists() || !resource.isReadable()) {
            throw new IllegalStateException("Required Cyrillic PDF font is missing or unreadable at startup: " + fontLocation);
        }
        try (InputStream is = resource.getInputStream()) {
            this.fontBytes = is.readAllBytes();
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to read Cyrillic PDF font from " + fontLocation, ex);
        }

        if (this.fontBytes == null || this.fontBytes.length < 10_000) {
            throw new IllegalStateException("Cyrillic PDF font file is empty or corrupted (" +
                    (this.fontBytes == null ? 0 : this.fontBytes.length) + " bytes) at " + fontLocation);
        }

        // Базова перевірка заголовка TrueType / OpenType шрифту
        boolean validTtfHeader = (fontBytes[0] == 0x00 && fontBytes[1] == 0x01 && fontBytes[2] == 0x00 && fontBytes[3] == 0x00)
                || (fontBytes[0] == 'O' && fontBytes[1] == 'T' && fontBytes[2] == 'T' && fontBytes[3] == 'O')
                || (fontBytes[0] == 't' && fontBytes[1] == 'r' && fontBytes[2] == 'u' && fontBytes[3] == 'e');

        if (!validTtfHeader) {
            throw new IllegalStateException("File at " + fontLocation + " does not have a valid TTF/OTF font header");
        }

        log.info("Verified Cyrillic PDF font at '{}' ({} bytes)", fontLocation, this.fontBytes.length);
    }

    public byte[] getFontBytes() {
        if (this.fontBytes == null || this.fontBytes.length == 0) {
            validateFontOnStartup();
        }
        return this.fontBytes;
    }
}