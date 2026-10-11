package com.liangshan.demo.service;

import java.util.LinkedHashMap;
import java.util.Map;

public final class FieldErrors {

    private final LinkedHashMap<String, String> errors = new LinkedHashMap<>();

    public void add(String field, String message) {
        if (message != null) {
            errors.putIfAbsent(field, message);
        }
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public void throwIfAny() {
        if (hasErrors()) {
            throw new FieldErrorsException(errors);
        }
    }

    public Map<String, String> asMap() {
        return errors;
    }
}
