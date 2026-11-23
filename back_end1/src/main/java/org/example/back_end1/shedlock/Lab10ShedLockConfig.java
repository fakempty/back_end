package org.example.back_end1.shedlock;

import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.time.Instant;

@Configuration
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "30s")
@ConditionalOnProperty(name = "lab10.shedlock.enabled", havingValue = "true", matchIfMissing = true)
public class Lab10ShedLockConfig {

    @Bean
    public LockProvider lockProvider(DataSource dataSource) {
        return new JdbcTemplateLockProvider(
                JdbcTemplateLockProvider.Configuration.builder()
                        .withJdbcTemplate(new JdbcTemplate(dataSource))
                        .usingDbTime()
                        .build()
        );
    }
}

@Component
@ConditionalOnProperty(name = "lab10.shedlock.enabled", havingValue = "true", matchIfMissing = true)
class Lab10ScheduledTask {

    private static final Logger log = LoggerFactory.getLogger(Lab10ScheduledTask.class);

    @Value("${server.port:8081}")
    private String instancePort;

    @Scheduled(fixedDelayString = "${lab10.shedlock.fixed-delay:15000}", initialDelay = 5000)
    @SchedulerLock(
            name = "lab10PartInventoryHeartbeatLock",
            lockAtMostFor = "${lab10.shedlock.lock-at-most-for:25s}",
            lockAtLeastFor = "${lab10.shedlock.lock-at-least-for:5s}"
    )
    public void runLockedInventoryCheck() {
        log.info("[Lab10 ShedLock] Executing scheduled task on instance port={} at {}", instancePort, Instant.now());
    }
}