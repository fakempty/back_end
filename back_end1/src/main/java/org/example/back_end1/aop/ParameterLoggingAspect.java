package org.example.back_end1.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class ParameterLoggingAspect {
    private static final Logger log = LoggerFactory.getLogger(ParameterLoggingAspect.class);

    @Before("@annotation(LogParameters)")
    public void logParameters(JoinPoint joinPoint) {
        log.info("[AOP Parameters] Виклик методу {} з аргументами: {}",
                joinPoint.getSignature().getName(), Arrays.toString(joinPoint.getArgs()));
    }
}