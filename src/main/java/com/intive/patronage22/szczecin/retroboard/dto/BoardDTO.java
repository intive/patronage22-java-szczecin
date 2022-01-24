package com.intive.patronage22.szczecin.retroboard.dto;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Value;


@Value
@Getter
@RequiredArgsConstructor
public class BoardDTO {
    public enum State {
        CREATED, VOTING, ACTIONS
    }

    String id;
    State state;
    String name;


}
