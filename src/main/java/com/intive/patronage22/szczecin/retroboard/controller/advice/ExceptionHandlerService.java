package com.intive.patronage22.szczecin.retroboard.controller.advice;

import com.google.firebase.auth.FirebaseAuthException;
import com.intive.patronage22.szczecin.retroboard.exception.BadRequestException;
import com.intive.patronage22.szczecin.retroboard.exception.NotFoundException;
import com.intive.patronage22.szczecin.retroboard.exception.UserAlreadyExistException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.validation.ConstraintViolationException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@ControllerAdvice
@ResponseBody
public class ExceptionHandlerService {

    @ResponseStatus(NOT_FOUND)
    @ExceptionHandler(NotFoundException.class)
    public String notFoundHandler(final NotFoundException exception) {
        return exception.getMessage();
    }

    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler(BadRequestException.class)
    public String badRequestHandler(final BadRequestException exception) {
        return exception.getMessage();
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ConstraintViolationException.class)
    public String constraintViolationHandler(final ConstraintViolationException exception) {
        return exception.getMessage();
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(UserAlreadyExistException.class)
    public String userAlreadyExistsHandler(final UserAlreadyExistException exception) {
        return exception.getMessage();
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public String methodArgumentNotValidHandler(final MethodArgumentNotValidException exception) {
        return exception.getMessage();
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(FirebaseAuthException.class)
    public String firebaseAuthHandler(final FirebaseAuthException exception) {
        return exception.getMessage();
    }
}
