package com.intive.patronage22.szczecin.retroboard.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Password cannot be empty")
public class PasswordFormatException extends RuntimeException {

    public PasswordFormatException() {
        super();
    }
}
