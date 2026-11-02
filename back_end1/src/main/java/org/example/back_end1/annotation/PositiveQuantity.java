package org.example.back_end1.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = {})
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@NotNull(message = "Кількість є обов'язковою")
@PositiveOrZero(message = "Кількість повинна бути нульовою або додатною")
public @interface PositiveQuantity {
    String message() default "Некоректна кількість товару";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}