package com.intive.patronage22.szczecin.retroboard.dto;

import com.intive.patronage22.szczecin.retroboard.model.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@Builder
@Value
@RequiredArgsConstructor
public class UserDto {

    @Schema(description = "User email")
    String email;
    @Schema(description = "User uid")
    String id;

    public static UserDto createFrom(final User user) {
        return UserDto.builder()
                .email(user.getEmail())
                .id(user.getUid())
                .build();
    }
}
