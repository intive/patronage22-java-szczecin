package com.intive.patronage22.szczecin.retroboard.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@Builder
@Getter
@RequiredArgsConstructor
public class BoardDto {

    String id;
    EnumStateDto state;
    String name;

    // convert Entity into DTO
    public static BoardDto mapToDto(Board board) {
        return BoardDto.builder()
                .id(board.getId().toString())
                .state(board.getState())
                .name(board.getName())
                .build();
    }
}
