package com.intive.patronage22.szczecin.retroboard.dto;

import com.intive.patronage22.szczecin.retroboard.exception.NotAcceptableException;
import lombok.AllArgsConstructor;

import java.util.Arrays;

@AllArgsConstructor
public enum EnumStateDto {

    CREATED(1), VOTING(2), ACTIONS(3), DONE(4);

    private final int enumValue;

    public EnumStateDto next() {
        if (enumValue < 4) {
            return Arrays.stream(values())
                    .filter(value -> value.enumValue == enumValue + 1)
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Board is not in one of the allowed states!"));
        } else {
            throw new NotAcceptableException("Cannot move to the next state!");
        }
    }
}