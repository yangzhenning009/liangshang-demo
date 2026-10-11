package com.liangshan.demo.web;

import com.liangshan.demo.service.FieldErrorsException;
import org.springframework.validation.BindingResult;

public final class Forms {

    private Forms() {
    }

    public static void reject(BindingResult binding, FieldErrorsException exception) {
        exception.getErrors().forEach((field, message) -> {
            if (field == null || field.isBlank() || "*".equals(field)) {
                binding.reject("rule", message);
                return;
            }
            try {
                binding.rejectValue(field, "rule", message);
            } catch (RuntimeException ignored) {
                binding.reject("rule", message);
            }
        });
    }
}
