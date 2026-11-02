package org.example.back_end1.annotation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.example.back_end1.dto.CreatePartRequest;
import org.example.back_end1.resolver.CurrentUserArgumentResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CustomAnnotationsTest {

    private Validator validator;
    private CurrentUserArgumentResolver resolver;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        resolver = new CurrentUserArgumentResolver();
    }

    // --- ТИП 1: AC1, AC2, AC3 (Bean Validation Constraint) ---

    @Test
    @DisplayName("AC2: Валідація проходить успішно, коли бізнес-правило ціни виконано")
    void validPartPricing_validData_noViolations() {
        CreatePartRequest req = new CreatePartRequest();
        req.setName("Свічка");
        req.setSku("SPK-1");
        req.setQuantity(150);
        req.setPrice(new BigDecimal("3000.00"));

        Set<ConstraintViolation<CreatePartRequest>> violations = validator.validate(req);
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("AC1, AC3: Валідатор фіксує порушення і повертає чітке повідомлення про помилку")
    void validPartPricing_bulkWithHighPrice_hasViolationWithClearMessage() {
        CreatePartRequest req = new CreatePartRequest();
        req.setName("Дорогий мотор");
        req.setSku("ENG-99");
        req.setQuantity(120);
        req.setPrice(new BigDecimal("9999.00"));

        Set<ConstraintViolation<CreatePartRequest>> violations = validator.validate(req);

        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("Для оптових партій (кількість > 100) ціна не може перевищувати 5000",
                violations.iterator().next().getMessage());
    }

    // --- ТИП 2: AC4, AC5 (Композитна анотація @PositiveQuantity) ---

    static class SampleStockDto {
        @PositiveQuantity
        private Integer stock;

        public SampleStockDto(Integer stock) {
            this.stock = stock;
        }
    }

    @Test
    @DisplayName("AC4, AC5: @PositiveQuantity валідує і @NotNull, і @PositiveOrZero одночасно")
    void positiveQuantity_compositeValidationBehavior() {
        SampleStockDto valid = new SampleStockDto(10);
        assertTrue(validator.validate(valid).isEmpty());

        SampleStockDto nullVal = new SampleStockDto(null);
        assertFalse(validator.validate(nullVal).isEmpty());

        SampleStockDto negativeVal = new SampleStockDto(-5);
        assertFalse(validator.validate(negativeVal).isEmpty());
    }

    // --- ТИП 3: AC6, AC7, AC8 (HandlerMethodArgumentResolver @CurrentUser) ---

    static class ControllerWithResolver {
        public void withAnnotation(@CurrentUser String username) {}
        public void withoutAnnotation(String username) {}
    }

    @Test
    @DisplayName("AC6: supportsParameter повертає true лише для параметрів з @CurrentUser")
    void supportsParameter_correctlyIdentifiesAnnotation() throws NoSuchMethodException {
        Method methodWith = ControllerWithResolver.class.getMethod("withAnnotation", String.class);
        Method methodWithout = ControllerWithResolver.class.getMethod("withoutAnnotation", String.class);

        MethodParameter paramWith = new MethodParameter(methodWith, 0);
        MethodParameter paramWithout = new MethodParameter(methodWithout, 0);

        assertTrue(resolver.supportsParameter(paramWith));
        assertFalse(resolver.supportsParameter(paramWithout));
    }

    @Test
    @DisplayName("AC7, AC8: resolveArgument повертає ім'я автентифікованого користувача")
    void resolveArgument_authenticatedUser_returnsUsername() throws NoSuchMethodException {
        Method method = ControllerWithResolver.class.getMethod("withAnnotation", String.class);
        MethodParameter parameter = new MethodParameter(method, 0);

        // Передаємо authorities, щоб токен отримав статус isAuthenticated() = true
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "john_doe",
                "password",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        Object result = resolver.resolveArgument(parameter, null, null, null);

        assertEquals("john_doe", result);
        SecurityContextHolder.clearContext();
    }
}
