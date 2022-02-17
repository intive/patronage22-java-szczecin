package com.intive.patronage22.szczecin.retroboard.repository;

import com.intive.patronage22.szczecin.retroboard.model.BoardCardVotes;
import com.intive.patronage22.szczecin.retroboard.model.BoardCardVotesKey;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BoardCardsVotesRepository extends CrudRepository<BoardCardVotes, BoardCardVotesKey> {

    Optional<BoardCardVotes> findByCardId(final Integer id);

    List<BoardCardVotes> findAllByIdCardId(final Integer cardId);
}


