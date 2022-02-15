package com.intive.patronage22.szczecin.retroboard.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@Value
@RequiredArgsConstructor
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
public class BoardCreateDto {

    @ApiModelProperty(value = "Board name", required = true, example = "My first board.")
    String name;
}
