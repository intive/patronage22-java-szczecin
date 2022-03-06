package com.intive.patronage22.szczecin.retroboard.exception;

import org.springframework.security.core.AuthenticationException;

public class EmailFormatException extends AuthenticationException {

    public EmailFormatException(final String msg) {
        super(msg);
    }
}
