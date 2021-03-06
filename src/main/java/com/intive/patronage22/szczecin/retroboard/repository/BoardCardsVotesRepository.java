package com.intive.patronage22.szczecin.retroboard.repository;

import com.intive.patronage22.szczecin.retroboard.model.Board;
import com.intive.patronage22.szczecin.retroboard.model.BoardCard;
import com.intive.patronage22.szczecin.retroboard.model.BoardCardVotes;
import com.intive.patronage22.szczecin.retroboard.model.BoardCardVotesKey;
import com.intive.patronage22.szczecin.retroboard.model.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BoardCardsVotesRepository extends CrudRepository<BoardCardVotes, BoardCardVotesKey> {

    Optional<BoardCardVotes> findByCardAndVoter(final BoardCard card, final User user);

    @Query(value = "SELECT bcv.votes FROM BoardCardVotes bcv JOIN BoardCard bc ON bcv.card.id = bc.id WHERE bc.board " +
                   "= :board AND bcv.voter=:user")
    List<Integer> getVotesByBoardAndUser(@Param("board") final Board board, @Param("user") final User user);

    @Query(value = "SELECT bcv.votes FROM BoardCardVotes bcv JOIN BoardCard bc ON bcv.card.id = bc.id WHERE bc.board " +
                   "= :board AND bc=:card AND bcv.voter=:user")
    Optional<Integer> getVotesByBoardAndCardAndUser(@Param("board") final Board board,
                                                    @Param("card") final BoardCard card,
                                                    @Param("user") final User user);

    @Query(value = "SELECT SUM(bcv.votes) FROM BoardCardVotes bcv JOIN BoardCard bc ON bcv.card.id = bc.id " +
                   "WHERE bc.board= :board AND bc=:card")
    Optional<Integer> getVotesByBoardAndCard(@Param("board") final Board board, @Param("card") final BoardCard card);
}