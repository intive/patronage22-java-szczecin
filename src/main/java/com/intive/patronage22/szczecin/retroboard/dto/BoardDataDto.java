package com.intive.patronage22.szczecin.retroboard.dto;

import com.intive.patronage22.szczecin.retroboard.model.Board;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import java.util.List;

@Builder
@Value
@RequiredArgsConstructor
public class BoardDataDto {

    @Schema(description = "Board data")
    Integer id;
    @Schema(description = "Board state", implementation = EnumStateDto.class)
    EnumStateDto state;
    @Schema(description = "Board name")
    String name;
    @Schema(description = "Board - number of votes")
    Integer numberOfVotes;
    @Schema(description = "Information about columns")
    List<BoardCardsColumnDto> columns;
    @Schema(description = "Assigned users")
    List<UserDto> users;

    public static BoardDataDto createFrom(final Board board, final List<BoardCardsColumnDto> boardCardsColumnDtos,
                                          final List<UserDto> usersList) {
        return BoardDataDto.builder()
                .id(board.getId())
                .state(board.getState())
                .name(board.getName())
                .numberOfVotes(board.getMaximumNumberOfVotes())
                .columns(boardCardsColumnDtos)
                .users(usersList)
                .build();
    }
}