package com.intive.patronage22.szczecin.retroboard.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value= HttpStatus.CONFLICT, reason = "user should not exist")
public class UsernameTakenException extends RuntimeException {

    public UsernameTakenException() {
        super();
    }
}
