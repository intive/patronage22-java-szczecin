package com.intive.patronage22.szczecin.retroboard.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT, reason = "User already exist")
public class UserAlreadyExistException extends RuntimeException {

    public UserAlreadyExistException() {
        super();
    }
}
