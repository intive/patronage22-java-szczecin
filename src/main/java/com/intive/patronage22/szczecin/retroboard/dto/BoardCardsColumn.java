package com.intive.patronage22.szczecin.retroboard.dto;

import com.intive.patronage22.szczecin.retroboard.exception.BadRequestException;
import lombok.Getter;

@Getter
public enum BoardCardsColumn {
    SUCCESS(0, "#238823"),
    FAILURES(1, "#D2222D"),
    KUDOS(2, "#283C77");

    private final int columnId;
    private final String colour;

    BoardCardsColumn(final int columnId, final String colour) {
        this.columnId = columnId;
        this.colour = colour;
    }

    public static BoardCardsColumn columnIdToBoardCardsColumn(final Integer columnId) {
        switch (columnId) {
            case 0:
                return BoardCardsColumn.SUCCESS;
            case 1:
                return BoardCardsColumn.FAILURES;
            case 2:
                return BoardCardsColumn.KUDOS;
            default:
                throw new BadRequestException("columnId can only have values between 0 and 2");
        }
    }
}
