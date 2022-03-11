package com.intive.patronage22.szczecin.retroboard.dto.exception;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.validation.FieldError;

@Value
@Builder
@RequiredArgsConstructor
public class ErrorResponse {

    @JsonProperty(value = "error_message")
    String message;

    public static ErrorResponse buildErrorResponse(Exception exception) {
        return ErrorResponse.builder()
                .message(exception.getMessage())
                .build();
    }

    public static ErrorResponse buildErrorResponse(FieldError fieldError) {
        return ErrorResponse.builder()
                .message(fieldError.getDefaultMessage())
                .build();
    }
}
