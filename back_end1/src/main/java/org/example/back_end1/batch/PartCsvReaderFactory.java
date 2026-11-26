package org.example.back_end1.batch;

import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.core.io.PathResource;

import java.nio.file.Path;

public final class PartCsvReaderFactory {

    private PartCsvReaderFactory() {}

    public static FlatFileItemReader<PartCsvRow> create(Path csvFilePath) {
        BeanWrapperFieldSetMapper<PartCsvRow> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(PartCsvRow.class);

        return new FlatFileItemReaderBuilder<PartCsvRow>()
                .name("partCsvItemReader")
                .resource(new PathResource(csvFilePath))
                .linesToSkip(1)
                .delimited()
                .delimiter(",")
                .names("sku", "name", "price", "quantity")
                .fieldSetMapper(fieldSetMapper)
                .build();
    }
}