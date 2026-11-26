package org.example.back_end1.batch;

import org.example.back_end1.auditing.BatchImportActorContext;
import org.example.back_end1.entity.Part;
import org.example.back_end1.repository.PartRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.nio.file.Path;

@Configuration
public class PartCsvBatchJobConfig {

    private static final Logger log = LoggerFactory.getLogger(PartCsvBatchJobConfig.class);
    public static final String IMPORT_IDENTITY = "CSV_IMPORT";

    @Bean
    public JobExecutionListener batchImportIdentityListener() {
        return new JobExecutionListener() {
            @Override
            public void beforeJob(JobExecution jobExecution) {
                BatchImportActorContext.setActor(IMPORT_IDENTITY);
                log.info("Starting CSV import jobExecutionId={} with actor={}",
                        jobExecution.getId(), IMPORT_IDENTITY);
            }

            @Override
            public void afterJob(JobExecution jobExecution) {
                log.info("Finished CSV import jobExecutionId={} status={}",
                        jobExecution.getId(), jobExecution.getStatus());
                BatchImportActorContext.clear();
            }
        };
    }

    @Bean
    @StepScope
    public FlatFileItemReader<PartCsvRow> partCsvItemReader(
            @Value("#{jobParameters['filePath']}") String filePath) {
        return PartCsvReaderFactory.create(Path.of(filePath));
    }

    @Bean
    @StepScope
    public PartCsvItemProcessor partCsvItemProcessor(PartRepository partRepository) {
        return new PartCsvItemProcessor(partRepository);
    }

    @Bean
    @StepScope
    public PartCsvItemWriter partCsvItemWriter(PartRepository partRepository) {
        return new PartCsvItemWriter(partRepository);
    }

    @Bean
    public Step importPartsStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            FlatFileItemReader<PartCsvRow> partCsvItemReader,
            PartCsvItemProcessor partCsvItemProcessor,
            PartCsvItemWriter partCsvItemWriter,
            @Value("${lab11.batch.chunk-size:5}") int chunkSize,
            @Value("${lab11.batch.skip-limit:10}") int skipLimit) {

        return new StepBuilder("importPartsStep", jobRepository)
                .<PartCsvRow, Part>chunk(chunkSize, transactionManager)
                .reader(partCsvItemReader)
                .processor(partCsvItemProcessor)
                .writer(partCsvItemWriter)
                .faultTolerant()
                .skip(FlatFileParseException.class)
                .skip(IllegalArgumentException.class)
                .skipLimit(skipLimit)
                .listener(new LoggingPartSkipListener())
                .build();
    }

    @Bean
    public Job importPartsCsvJob(
            JobRepository jobRepository,
            Step importPartsStep,
            JobExecutionListener batchImportIdentityListener) {

        return new JobBuilder("importPartsCsvJob", jobRepository)
                .listener(batchImportIdentityListener)
                .start(importPartsStep)
                .build();
    }
}