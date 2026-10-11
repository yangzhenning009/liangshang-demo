package com.liangshan.demo.service;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class FieldErrorsException extends RuntimeException {

    private final Map<String, String> errors;

    public FieldErrorsException(Map<String, String> errors) {
        super(String.join("；", errors.values()));
        this.errors = Collections.unmodifiableMap(new LinkedHashMap<>(errors));
    }

    public Map<String, String> getErrors() {
        return errors;
    }
}
