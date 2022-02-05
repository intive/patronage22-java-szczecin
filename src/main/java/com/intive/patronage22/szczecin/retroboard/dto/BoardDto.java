package com.intive.patronage22.szczecin.retroboard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class BoardDto {


    Integer id;
    EnumStateDto state;
    String name;


}
