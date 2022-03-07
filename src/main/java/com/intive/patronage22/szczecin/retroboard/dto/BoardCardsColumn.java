package com.intive.patronage22.szczecin.retroboard.dto;

public enum BoardCardsColumn {
    SUCCESS(0, "#238823"),
    FAILURES(1, "#D2222D"),
    KUDOS(2, "#283C77");

    public final int orderNumber;
    public final String colour;

    BoardCardsColumn(final int orderNumber, final String colour){
        this.orderNumber = orderNumber;
        this.colour = colour;
    }
}
