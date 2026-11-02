package org.example.back_end1.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ExecutionTimeAspectTest {

    @Test
    void logExecutionTime_proceedsAndLogsSuccessfully() throws Throwable {
        ExecutionTimeAspect aspect = new ExecutionTimeAspect();
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        Signature signature = mock(Signature.class);

        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.toString()).thenReturn("testMethod()");
        when(joinPoint.proceed()).thenReturn("MockedResult");

        Object result = aspect.logExecutionTime(joinPoint);

        assertEquals("MockedResult", result);
        verify(joinPoint, times(1)).proceed();
    }
}