package org.example.back_end1.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.SkipListener;
import org.springframework.batch.item.file.FlatFileParseException;
import org.example.back_end1.entity.Part;

public class LoggingPartSkipListener implements SkipListener<PartCsvRow, Part> {

    private static final Logger log = LoggerFactory.getLogger(LoggingPartSkipListener.class);

    @Override
    public void onSkipInRead(Throwable t) {
        if (t instanceof FlatFileParseException parseEx) {
            log.warn("Skipped CSV line {} due to parse error: {} | raw='{}'",
                    parseEx.getLineNumber(), parseEx.getMessage(), parseEx.getInput());
        } else {
            log.warn("Skipped CSV row during read: {}", t.getMessage());
        }
    }

    @Override
    public void onSkipInProcess(PartCsvRow item, Throwable t) {
        String sku = item != null ? item.getSku() : "unknown";
        log.warn("Skipped CSV item during processing (sku={}): {}", sku, t.getMessage());
    }

    @Override
    public void onSkipInWrite(Part item, Throwable t) {
        String sku = item != null ? item.getSku() : "unknown";
        log.warn("Skipped CSV item during write (sku={}): {}", sku, t.getMessage());
    }
}