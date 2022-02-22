package com.intive.patronage22.szczecin.retroboard.controller.advice;

import com.intive.patronage22.szczecin.retroboard.exception.BadRequestException;
import com.intive.patronage22.szczecin.retroboard.exception.BoardNotFoundException;
import com.intive.patronage22.szczecin.retroboard.exception.MissingPermissionsException;
import com.intive.patronage22.szczecin.retroboard.exception.UserNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@ControllerAdvice
@ResponseBody
public class ExceptionHandlerService {

    @ResponseStatus(NOT_FOUND)
    @ExceptionHandler(UserNotFoundException.class)
    public String userNotFoundHandler(final UserNotFoundException exception) {
        return exception.getMessage();
    }

    @ResponseStatus(NOT_FOUND)
    @ExceptionHandler(BoardNotFoundException.class)
    public String boardNotFoundHandler(final BoardNotFoundException exception) {
        return exception.getMessage();
    }

    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler(MissingPermissionsException.class)
    public String missingPermissionsHandler(final MissingPermissionsException exception) {
        return exception.getMessage();
    }

    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler(BadRequestException.class)
    public String badRequestHandler(final BadRequestException exception) {
        return exception.getMessage();
    }
}
