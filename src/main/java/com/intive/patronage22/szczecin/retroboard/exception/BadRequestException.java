package com.intive.patronage22.szczecin.retroboard.exception;

public class BadRequestException extends RuntimeException {

    private static final String MESSAGE = "Bad request.";

    public BadRequestException() {
        super(MESSAGE);
    }
}
