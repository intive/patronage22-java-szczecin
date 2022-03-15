package com.intive.patronage22.szczecin.retroboard.dto;

import com.intive.patronage22.szczecin.retroboard.model.BoardCard;
import com.intive.patronage22.szczecin.retroboard.model.BoardCardAction;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.stream.Collectors;

@Builder
@Value
@RequiredArgsConstructor
public class BoardCardDto {

    @Schema(description = "Board card id")
    Integer id;

    @NotEmpty
    @NotBlank
    @Size(min = 5, max = 128)
    @Schema(description = "Board card text")
    String cardText;

    @NotNull
    @Min(0)
    @Max(2)
    @Schema(description = "Board card column id")
    Integer columnId;

    @Schema(description = "Board card creator")
    String boardCardCreator;

    @Schema(description = "Board card action text")
    List<String> actionTexts;

    public static BoardCardDto createFrom(final BoardCard boardCard) {
        return BoardCardDto.builder()
                .id(boardCard.getId())
                .cardText(boardCard.getText())
                .columnId(boardCard.getColumn().getColumnId())
                .boardCardCreator(boardCard.getCreator().getEmail())
                .actionTexts(
                        boardCard.getBoardCardActions().stream().map(BoardCardAction::getText)
                                .collect(Collectors.toList())).build();
    }
}
