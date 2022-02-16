package com.intive.patronage22.szczecin.retroboard.dto;

import com.intive.patronage22.szczecin.retroboard.model.Board;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@Builder
@Value
@RequiredArgsConstructor
public class BoardDto {

    @ApiModelProperty(value = "Board id", name = "id", example = "1")
    Integer id;
    @ApiModelProperty(value = "Board state", example = "CREATED")
    EnumStateDto state;
    @ApiModelProperty(value = "Board name", required = true, example = "My first board.")
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
