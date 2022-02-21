package com.intive.patronage22.szczecin.retroboard.exception;

import org.springframework.security.core.AuthenticationException;

public class MissingFieldException extends AuthenticationException {

    public MissingFieldException(final String msg) {
        super(msg);
    }
}
