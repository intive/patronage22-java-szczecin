package com.intive.patronage22.szczecin.retroboard.exception;

public class UserNotFoundException extends RuntimeException {

    private static final String MESSAGE = "User not found";

    public UserNotFoundException() {
        super(MESSAGE);
    }
}
