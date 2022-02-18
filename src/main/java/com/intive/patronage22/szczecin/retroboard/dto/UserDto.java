package com.intive.patronage22.szczecin.retroboard.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class UserDto {
    private String idToken;
    private String email;
    private String refreshToken;
    private String expiresIn;
    private String localId;
    private boolean registered;
}
