package org.example.back_end1.batch;

import org.example.back_end1.entity.Part;
import org.example.back_end1.repository.PartRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

public class PartCsvItemWriter implements ItemWriter<Part> {

    private static final Logger log = LoggerFactory.getLogger(PartCsvItemWriter.class);
    private final PartRepository partRepository;

    public PartCsvItemWriter(PartRepository partRepository) {
        this.partRepository = partRepository;
    }

    @Override
    public void write(Chunk<? extends Part> chunk) {
        if (chunk == null || chunk.isEmpty()) {
            log.warn("PartCsvItemWriter received empty chunk");
            return;
        }
        var skus = chunk.getItems().stream().map(Part::getSku).toList();
        log.info("Writing chunk size={} skus={}", chunk.size(), skus);
        partRepository.saveAll(chunk.getItems());
        partRepository.flush();
    }
}