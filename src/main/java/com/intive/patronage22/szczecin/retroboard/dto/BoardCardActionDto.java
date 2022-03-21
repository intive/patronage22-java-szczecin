package com.intive.patronage22.szczecin.retroboard.dto;

import com.intive.patronage22.szczecin.retroboard.model.BoardCardAction;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@Builder
@Value
@RequiredArgsConstructor
public class BoardCardActionDto {

    @Schema(description = "Board card action id")
    int id;

    @Schema(description = "Board card id")
    int cardId;

    @Schema(description = "Board card text")
    String text;

    public static BoardCardActionDto createFrom(final BoardCardAction boardCardAction) {
        return BoardCardActionDto.builder()
                .id(boardCardAction.getId())
                .cardId(boardCardAction.getCard().getId())
                .text(boardCardAction.getText())
                .build();
    }
}