package com.intive.patronage22.szczecin.retroboard.exception;

public class UserIsNotOwnerException extends RuntimeException {

    private static final String MESSAGE = "User is not owner";

    public UserIsNotOwnerException() {
        super(MESSAGE);
    }
}
