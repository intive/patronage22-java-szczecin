package com.intive.patronage22.szczecin.retroboard.dto;

import lombok.Getter;

@Getter
public class UserLoginRequestDto {
    private final String email;
    private final String password;
    private final boolean returnSecureToken = true;

    public UserLoginRequestDto(final String email, final String password) {
        this.email = email;
        this.password = password;
    }
}
