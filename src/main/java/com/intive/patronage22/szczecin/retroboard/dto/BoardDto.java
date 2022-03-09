package com.intive.patronage22.szczecin.retroboard.dto;

import com.intive.patronage22.szczecin.retroboard.model.Board;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Builder
@Value
@RequiredArgsConstructor
public class BoardDto {

    @Schema(description = "Board id")
    Integer id;
    @Schema(description = "Board state", implementation = EnumStateDto.class)
    EnumStateDto state;

    @NotEmpty
    @NotBlank
    @Size(min = 4, max = 64)
    @Schema(description = "Board name", required = true)
    String name;
    @Schema(description = "Maximum number of votes")
    Integer numberOfVotes;

    // convert Entity into DTO
    public static BoardDto fromModel(final Board board) {
        return BoardDto.builder()
                .id(board.getId())
                .state(board.getState())
                .name(board.getName())
                .numberOfVotes(board.getMaximumNumberOfVotes())
                .build();
    }
}
