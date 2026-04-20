package com.busticketbookingsystem.payment.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Bean Validation tests for PaymentRequestDTO.
 * Confirms the constraints added after moving from no-validation to @Valid.
 */
class PaymentRequestDTOValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void init() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void close() {
        factory.close();
    }

    private Set<String> propertiesWithErrors(PaymentRequestDTO dto) {
        return validator.validate(dto).stream()
                .map(ConstraintViolation::getPropertyPath)
                .map(Object::toString)
                .collect(Collectors.toSet());
    }

    @Test
    @DisplayName("POSITIVE: fully populated DTO produces no violations")
    void valid() {
        PaymentRequestDTO dto = new PaymentRequestDTO(1, 2, new BigDecimal("500.00"));
        assertTrue(validator.validate(dto).isEmpty());
    }

    @Test
    @DisplayName("NEGATIVE: null bookingId, customerId, amount all flagged")
    void allNull() {
        Set<String> errors = propertiesWithErrors(new PaymentRequestDTO(null, null, null));
        assertTrue(errors.contains("bookingId"));
        assertTrue(errors.contains("customerId"));
        assertTrue(errors.contains("amount"));
    }

    @Test
    @DisplayName("NEGATIVE: non-positive bookingId and customerId are flagged")
    void nonPositiveIds() {
        Set<String> errors = propertiesWithErrors(
                new PaymentRequestDTO(0, -5, new BigDecimal("100.00")));
        assertTrue(errors.contains("bookingId"));
        assertTrue(errors.contains("customerId"));
    }

    @Test
    @DisplayName("NEGATIVE: zero amount fails @DecimalMin('0.01')")
    void zeroAmount() {
        Set<String> errors = propertiesWithErrors(
                new PaymentRequestDTO(1, 2, BigDecimal.ZERO));
        assertTrue(errors.contains("amount"));
    }

    @Test
    @DisplayName("NEGATIVE: negative amount fails @DecimalMin")
    void negativeAmount() {
        Set<String> errors = propertiesWithErrors(
                new PaymentRequestDTO(1, 2, new BigDecimal("-10.00")));
        assertTrue(errors.contains("amount"));
    }

    @Test
    @DisplayName("POSITIVE: amount exactly 0.01 passes")
    void minAmountBoundary() {
        PaymentRequestDTO dto = new PaymentRequestDTO(1, 2, new BigDecimal("0.01"));
        assertTrue(validator.validate(dto).isEmpty());
    }
}
