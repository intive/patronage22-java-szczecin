package com.intive.patronage22.szczecin.retroboard.dto;
import com.intive.patronage22.szczecin.retroboard.model.BoardCard;
import com.intive.patronage22.szczecin.retroboard.model.BoardCardAction;
import com.intive.patronage22.szczecin.retroboard.model.BoardCardVotes;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.util.List;

@Builder
@Value
@RequiredArgsConstructor
public class BoardCardDataDto {

    @Schema(description = "Board card id") Integer id;
    @Schema(description = "Board card text") String cardText;
    @Schema(description = "Board card column name", implementation = BoardCardsColumn.class) BoardCardsColumn
            columnName;
    @Schema(description = "Board card creator") String boardCardCreator;
    @Schema(description = "Board card action text") String actionText;
    @Schema(description = "Board card number of votes") Integer votesNumber;
    @Schema(description = "Board card voters name list") List<BoardCardVotesDto> voters;

    public static BoardCardDataDto create(final BoardCard boardCard, final BoardCardAction boardCardAction,
                                             final BoardCardVotes boardCardVotes,
                                             final List<BoardCardVotesDto> voters) {
        return BoardCardDataDto.builder()
                .id(boardCard.getId())
                .cardText(boardCard.getText())
                .columnName(boardCard.getColumn())
                .boardCardCreator(boardCard.getCreator().getName())
                .actionText(boardCardAction.getText())
                .votesNumber(boardCardVotes.getVotes())
                .voters(voters).build();
    }
}
