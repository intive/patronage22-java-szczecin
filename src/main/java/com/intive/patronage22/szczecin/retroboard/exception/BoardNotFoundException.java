package com.intive.patronage22.szczecin.retroboard.exception;

public class BoardNotFoundException extends RuntimeException {

    private static final String MESSAGE = "Board not found";

    public BoardNotFoundException() {
        super(MESSAGE);
    }
}