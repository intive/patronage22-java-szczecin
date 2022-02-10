package com.intive.patronage22.szczecin.retroboard.dto;

import com.intive.patronage22.szczecin.retroboard.model.Board;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Builder
@Getter
@RequiredArgsConstructor
public class BoardDto {
    private final Integer id;
    private final EnumStateDto state;
    private final String name;

    public static BoardDto fromModel(Board board) {
        return BoardDto.builder()
                .id(board.getId())
                .name(board.getName())
                .state(board.getState()).build();
    }
}
