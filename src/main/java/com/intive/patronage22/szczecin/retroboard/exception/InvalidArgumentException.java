package com.intive.patronage22.szczecin.retroboard.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.validation.FieldError;

import java.util.List;

@Getter
@AllArgsConstructor
public class InvalidArgumentException extends RuntimeException {
    private static final String DEFAULT_MESSAGE = "Invalid argument";

    private final List<FieldError> fieldErrors;
}

