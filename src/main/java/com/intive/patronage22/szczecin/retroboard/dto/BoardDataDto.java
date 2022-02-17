package com.intive.patronage22.szczecin.retroboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.util.List;

@Builder
@Value
@RequiredArgsConstructor
public class BoardDataDto {

    @Schema(description = "Board data") BoardDto board;
    @Schema(description = "Board cards data") List<BoardCardDataDto> boardCards;

    public static BoardDataDto create(final BoardDto boardDto, List<BoardCardDataDto> boardCards) {
        return BoardDataDto.builder()
                .board(boardDto)
                .boardCards(boardCards)
                .build();
    }
}
