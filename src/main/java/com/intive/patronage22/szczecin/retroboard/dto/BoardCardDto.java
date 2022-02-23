package com.intive.patronage22.szczecin.retroboard.dto;

import com.intive.patronage22.szczecin.retroboard.model.BoardCardAction;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import java.util.List;
import java.util.stream.Collectors;

@Builder
@Value
@RequiredArgsConstructor
public class BoardCardDto {

    @Schema(description = "Board card id")
    Integer id;
    @Schema(description = "Board card text")
    String cardText;
    @Schema(description = "Board card column name", implementation = BoardCardsColumn.class)
    BoardCardsColumn columnName;
    @Schema(description = "Board card creator")
    String boardCardCreator;
    @Schema(description = "Board card action text")
    List<String> actionTexts;

    public static BoardCardDto createFrom(final com.intive.patronage22.szczecin.retroboard.model.BoardCard boardCard) {
        return BoardCardDto.builder()
                .id(boardCard.getId())
                .cardText(boardCard.getText())
                .columnName(boardCard.getColumn())
                .boardCardCreator(boardCard.getCreator().getName())
                .actionTexts(
                        boardCard.getBoardCardActions().stream().map(BoardCardAction::getText)
                                .collect(Collectors.toList())).build();
    }
}