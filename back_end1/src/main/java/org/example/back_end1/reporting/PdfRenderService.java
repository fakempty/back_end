package org.example.back_end1.reporting;

import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
public class PdfRenderService {

    private static final String TEMPLATE_PREFIX = "reporting/";
    private static final byte[] PDF_MAGIC_HEADER = new byte[]{'%', 'P', 'D', 'F', '-'};

    private final Configuration freemarkerConfig;
    private final ReportingFontService fontService;

    public PdfRenderService(Configuration freemarkerConfig, ReportingFontService fontService) {
        this.freemarkerConfig = freemarkerConfig;
        this.fontService = fontService;
    }

    public byte[] renderPdfFromTemplate(String templateName, Map<String, Object> model) {
        if (templateName == null || templateName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Report template name must not be blank");
        }

        String html;
        try {
            Template template = freemarkerConfig.getTemplate(TEMPLATE_PREFIX + templateName, StandardCharsets.UTF_8.name());
            StringWriter out = new StringWriter();
            template.process(model, out);
            html = out.toString();
        } catch (Exception ex) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to render FreeMarker template '" + templateName + "': " + ex.getMessage(),
                    ex
            );
        }

        if (html == null || html.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Rendered HTML for template '" + templateName + "' is empty"
            );
        }

        byte[] fontBytes = fontService.getFontBytes();

        try (ByteArrayOutputStream pdfOut = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.useFont(() -> new ByteArrayInputStream(fontBytes), "DejaVu Sans", 400,
                    BaseRendererBuilder.FontStyle.NORMAL, true);
            builder.useFont(() -> new ByteArrayInputStream(fontBytes), "DejaVu Sans", 700,
                    BaseRendererBuilder.FontStyle.NORMAL, true);
            builder.withHtmlContent(html, null);
            builder.toStream(pdfOut);
            builder.run();

            byte[] pdfBytes = pdfOut.toByteArray();
            if (!isValidPdf(pdfBytes)) {
                throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Generated PDF output is empty or invalid"
                );
            }
            return pdfBytes;
        } catch (ResponseStatusException rse) {
            throw rse;
        } catch (Exception ex) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "PDF rendering failed for template '" + templateName + "': " + ex.getMessage(),
                    ex
            );
        }
    }

    private boolean isValidPdf(byte[] bytes) {
        if (bytes == null || bytes.length < 256) {
            return false;
        }
        for (int i = 0; i < PDF_MAGIC_HEADER.length; i++) {
            if (bytes[i] != PDF_MAGIC_HEADER[i]) {
                return false;
            }
        }
        String tail = new String(bytes, Math.max(0, bytes.length - 128), Math.min(128, bytes.length), StandardCharsets.ISO_8859_1);
        return tail.contains("%%EOF");
    }
}