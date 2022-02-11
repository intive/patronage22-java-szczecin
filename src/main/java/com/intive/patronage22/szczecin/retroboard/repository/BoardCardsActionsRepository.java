package com.intive.patronage22.szczecin.retroboard.repository;

import com.intive.patronage22.szczecin.retroboard.model.BoardCardAction;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BoardCardsActionsRepository
        extends CrudRepository<BoardCardAction, Integer> {
}

