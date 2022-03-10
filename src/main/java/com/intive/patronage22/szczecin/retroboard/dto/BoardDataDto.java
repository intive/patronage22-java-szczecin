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

    @Schema(description = "Board data")
    BoardDto board;
    @Schema(description = "Information about columns")
    List<BoardCardsColumnDto> columns;
    @Schema(description = "Assigned users")
    List<UserDto> users;

    public static BoardDataDto createFrom(final BoardDto boardDto, final List<BoardCardsColumnDto> boardCardsColumnDtos,
                                          final List<UserDto> usersList) {
        return BoardDataDto.builder()
                .board(boardDto)
                .columns(boardCardsColumnDtos)
                .users(usersList)
                .build();
    }
}