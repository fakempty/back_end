package org.example.back_end1.service;

import org.example.back_end1.aop.LogExecutionTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SelfInvocationDemoService {
    private static final Logger log = LoggerFactory.getLogger(SelfInvocationDemoService.class);

    // Зовнішній виклик (Аспект СПРАЦЬОВУЄ)
    @LogExecutionTime
    public void externalCall() {
        log.info("Виконано externalCall()");
    }

    // Метод, який викликає інший анотований метод через цей же клас (Самовиклик)
    public void selfInvocationTrigger() {
        log.info("Виклик всередині класу через this...");
        this.internalAnnotatedMethod(); // САМУВИКЛИК: Аспект НЕ спрацює!
    }

    @LogExecutionTime
    public void internalAnnotatedMethod() {
        log.info("Виконано internalAnnotatedMethod() через самовиклик.");
    }
}