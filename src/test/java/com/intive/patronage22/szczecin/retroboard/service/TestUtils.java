package com.intive.patronage22.szczecin.retroboard.service;

import com.intive.patronage22.szczecin.retroboard.dto.EnumStateDto;
import com.intive.patronage22.szczecin.retroboard.model.Board;
import com.intive.patronage22.szczecin.retroboard.model.User;

import java.util.Set;

public class TestUtils {

    public static Board buildBoard(final int id, final EnumStateDto state, final User user, final Set<User> users, final Integer maximumNumberOfVotes) {
        return Board.builder()
                .id(id)
                .name("My first board.")
                .state(state)
                .creator(user)
                .users(users)
                .maximumNumberOfVotes(maximumNumberOfVotes)
                .build();
    }
}