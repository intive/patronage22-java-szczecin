package com.intive.patronage22.szczecin.retroboard.exception;

public class MissingPermissionsException extends RuntimeException {

    private static final String MESSAGE = "User has no permission to view data.";

    public MissingPermissionsException() {
        super(MESSAGE);
    }

}
