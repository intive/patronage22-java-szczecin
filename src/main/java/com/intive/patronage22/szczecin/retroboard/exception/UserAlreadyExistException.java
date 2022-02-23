package com.intive.patronage22.szczecin.retroboard.exception;

public class UserAlreadyExistException extends RuntimeException {

    public UserAlreadyExistException(final String message) {
        super(message);
    }
}
