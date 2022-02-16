package com.intive.patronage22.szczecin.retroboard.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Email format not valid")
public class EmailFormatException extends RuntimeException {

    public EmailFormatException() {
        super();
    }
}
