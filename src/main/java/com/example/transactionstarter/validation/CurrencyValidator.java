package com.example.transactionstarter.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Currency;

public class CurrencyValidator implements ConstraintValidator<ValidCurrency, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.trim().isEmpty()) {
            // Null/blank checks are handled by @NotBlank/@NotNull
            return true;
        }

        String trimmed = value.trim();
        if (trimmed.length() != 3) {
            return false;
        }

        try {
            Currency currency = Currency.getInstance(trimmed.toUpperCase());
            return currency != null;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
