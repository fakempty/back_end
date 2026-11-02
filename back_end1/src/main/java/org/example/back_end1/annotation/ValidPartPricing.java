package org.example.back_end1.annotation;

import org.example.back_end1.validation.PartPricingValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PartPricingValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPartPricing {
    String message() default "Для оптових партій (кількість > 100) ціна не може перевищувати 5000";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}