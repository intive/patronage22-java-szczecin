package com.intive.patronage22.szczecin.retroboard.repository;

import com.intive.patronage22.szczecin.retroboard.model.Board;
import com.intive.patronage22.szczecin.retroboard.model.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BoardRepository extends CrudRepository<Board, Integer> {

    @Query(value = "SELECT b FROM Board b WHERE b.id=:id AND (b.creator=:user OR :user IN elements(b.users))")
    Optional<Board> findBoardByIdAndCreatorOrAssignedUser(@Param("id") final Integer id,
                                                          @Param("user") final User user);

    @Query(value = "SELECT b FROM Board b WHERE (b.creator=:user or :user IN elements(b.users))")
    Optional<List<Board>> findBoardByCreatorOrAssignedUser(@Param("user") final User user);
}
