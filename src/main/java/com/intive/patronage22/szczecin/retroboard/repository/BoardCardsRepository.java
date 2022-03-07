package com.intive.patronage22.szczecin.retroboard.repository;

import com.intive.patronage22.szczecin.retroboard.model.BoardCard;
import com.intive.patronage22.szczecin.retroboard.model.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BoardCardsRepository extends CrudRepository<BoardCard, Integer> {

    List<BoardCard> findAllByCreatorOrderByIdAsc(final User user);

    List<BoardCard> findAllByBoardIdOrderByIdAsc(final Integer id);
}
