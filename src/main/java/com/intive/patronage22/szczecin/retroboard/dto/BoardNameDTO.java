package com.intive.patronage22.szczecin.retroboard.dto;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
public class BoardNameDTO {

    String name;
}
