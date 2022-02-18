package com.intive.patronage22.szczecin.retroboard.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "DisplayName cannot be empty")
public class DisplayNameFormatException extends RuntimeException {

    public DisplayNameFormatException() {
        super();
    }
}
