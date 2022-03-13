package com.intive.patronage22.szczecin.retroboard.dto;

import com.intive.patronage22.szczecin.retroboard.exception.BadRequestException;
import lombok.Getter;

@Getter
public enum BoardCardsColumn {
    SUCCESS(0, "#238823"),
    FAILURES(1, "#D2222D"),
    KUDOS(2, "#283C77");

    private final int orderNumber;
    private final String colour;

    BoardCardsColumn(final int orderNumber, final String colour) {
        this.orderNumber = orderNumber;
        this.colour = colour;
    }

    public static BoardCardsColumn orderNumberToBoardCardsColumn(final Integer orderNumber) {
        switch (orderNumber) {
            case 0:
                return BoardCardsColumn.SUCCESS;
            case 1:
                return BoardCardsColumn.FAILURES;
            case 2:
                return BoardCardsColumn.KUDOS;
            default:
                throw new BadRequestException("orderNumber can only have values between 0 and 2");
        }
    }
}
