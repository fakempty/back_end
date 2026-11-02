package org.example.back_end1.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AuditAspect {
    private static final Logger log = LoggerFactory.getLogger(AuditAspect.class);

    @AfterReturning(pointcut = "@annotation(auditAction)", returning = "result")
    public void auditMethod(JoinPoint joinPoint, AuditAction auditAction, Object result) {
        log.info("[AOP AuditAction] Дія '{}' успішно виконана в методі: {}. Результат: {}",
                auditAction.value(), joinPoint.getSignature().getName(), result);
    }
}