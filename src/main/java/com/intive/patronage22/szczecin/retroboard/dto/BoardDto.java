package com.intive.patronage22.szczecin.retroboard.dto;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Value;


@Value
@Getter
@RequiredArgsConstructor
public class BoardDto {


    String id;
    EnumStateDto state;
    String name;


}
