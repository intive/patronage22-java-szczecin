package com.intive.patronage22.szczecin.retroboard.dto;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.util.List;

@Builder
@Value
@RequiredArgsConstructor
public class BoardDataDto {

    BoardDto board;
    List<BoardCardDataDto> boardCards;

    public static BoardDataDto fromModel() {
        return BoardDataDto.builder().build();
    }
}
