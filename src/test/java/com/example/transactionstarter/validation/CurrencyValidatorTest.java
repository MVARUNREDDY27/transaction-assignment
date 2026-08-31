package com.example.transactionstarter.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CurrencyValidatorTest {

    private CurrencyValidator validator;

    @BeforeEach
    void setUp() {
        validator = new CurrencyValidator();
    }

    @Test
    @DisplayName("Valid ISO-4217 currency codes are accepted")
    void validCurrencies() {
        assertTrue(validator.isValid("USD", null));
        assertTrue(validator.isValid("EUR", null));
        assertTrue(validator.isValid("GBP", null));
        assertTrue(validator.isValid("INR", null));
        assertTrue(validator.isValid("JPY", null));
        assertTrue(validator.isValid("usd", null)); // lower case handled
    }

    @Test
    @DisplayName("Invalid or non-existent ISO-4217 currency codes are rejected")
    void invalidCurrencies() {
        assertFalse(validator.isValid("XYZ", null));
        assertFalse(validator.isValid("US", null)); // only 2 chars
        assertFalse(validator.isValid("USDD", null)); // 4 chars
        assertFalse(validator.isValid("123", null));
        assertFalse(validator.isValid("ABC", null));
    }

    @Test
    @DisplayName("Null or empty values pass validator because @NotBlank handles presence")
    void nullOrEmptyValues() {
        assertTrue(validator.isValid(null, null));
        assertTrue(validator.isValid("", null));
        assertTrue(validator.isValid("   ", null));
    }
}
