package com.intive.patronage22.szczecin.retroboard.dto;

import com.intive.patronage22.szczecin.retroboard.model.Board;
import lombok.Builder;
import lombok.RequiredArgsConstructor;

@Builder
@Value
@RequiredArgsConstructor
public class BoardDto {
    Integer id;
    EnumStateDto state;
    String name;

    // convert Entity into DTO
    public static BoardDto mapToDto(Board board) {
        return BoardDto.builder()
                .id(board.getId())
                .state(board.getState())
                .name(board.getName())
                .build();
    }
}