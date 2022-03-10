package com.intive.patronage22.szczecin.retroboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@Builder
@Value
@RequiredArgsConstructor
public class BoardCardsColumnDto {

    @Schema(description = "Column name")
    String name;
    @Schema(description = "Column id")
    Integer id;
    @Schema(description = "Column position")
    Integer position;
    @Schema(description = "Column colour")
    String colour;

    public static BoardCardsColumnDto createFrom(final BoardCardsColumn boardCardsColumn) {
        return BoardCardsColumnDto.builder()
                .name(boardCardsColumn.name())
                .id(boardCardsColumn.getOrderNumber())
                .position(boardCardsColumn.getOrderNumber())
                .colour(boardCardsColumn.getColour())
                .build();
    }
}
