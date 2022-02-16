package com.intive.patronage22.szczecin.retroboard.dto;

import com.intive.patronage22.szczecin.retroboard.model.Board;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@Builder
@Value
@RequiredArgsConstructor
public class BoardDto {

    @Schema(description = "Board id")
    Integer id;
    @Schema(description = "Board state", implementation = EnumStateDto.class)
    EnumStateDto state;
    @Schema(description = "Board name", required = true)
    String name;

    // convert Entity into DTO
    public static BoardDto fromModel(Board board) {
        return BoardDto.builder()
                .id(board.getId())
                .state(board.getState())
                .name(board.getName())
                .build();
    }
}
