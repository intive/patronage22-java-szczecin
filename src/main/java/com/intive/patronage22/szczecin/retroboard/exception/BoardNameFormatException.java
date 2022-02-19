package com.intive.patronage22.szczecin.retroboard.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "BoardName cannot be empty")
public class BoardNameFormatException extends RuntimeException {

    public BoardNameFormatException() {
        super();
    }
}
