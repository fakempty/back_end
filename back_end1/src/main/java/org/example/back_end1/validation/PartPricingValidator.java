package org.example.back_end1.validation;

import org.example.back_end1.annotation.ValidPartPricing;
import org.example.back_end1.dto.CreatePartRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.math.BigDecimal;

public class PartPricingValidator implements ConstraintValidator<ValidPartPricing, CreatePartRequest> {

    @Override
    public boolean isValid(CreatePartRequest request, ConstraintValidatorContext context) {
        if (request == null) {
            return true;
        }

        int quantity = request.getQuantity();
        BigDecimal priceDecimal = request.getPrice();

        // Безпечне переведення BigDecimal у double для порівняння
        double price = priceDecimal != null ? priceDecimal.doubleValue() : 0.0;

        // Повертає false (помилку валідації), якщо кількість > 100 і ціна > 5000
        return !(quantity > 100 && price > 5000.0);
    }
}