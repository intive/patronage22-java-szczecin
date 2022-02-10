package com.intive.patronage22.szczecin.retroboard.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Builder
@Getter
@RequiredArgsConstructor
public class BoardDto {
    final Integer id;
    final EnumStateDto state;
    final String name;
}
