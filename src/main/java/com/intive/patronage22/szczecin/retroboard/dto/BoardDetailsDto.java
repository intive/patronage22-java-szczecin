package com.intive.patronage22.szczecin.retroboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.util.List;

@Builder
@Value
@RequiredArgsConstructor
public class BoardDetailsDto {

    @Schema(description = "Board column id")
    Integer id;
    @Schema(description = "List of board cards")
    List<BoardCardDto> boardCards;

    public static BoardDetailsDto createFrom(final Integer columnId, List<BoardCardDto> boardCards) {
        return BoardDetailsDto.builder()
                .id(columnId)
                .boardCards(boardCards)
                .build();
    }
}

