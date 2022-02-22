package com.intive.patronage22.szczecin.retroboard.service;

import com.intive.patronage22.szczecin.retroboard.dto.BoardDto;
import com.intive.patronage22.szczecin.retroboard.dto.EnumStateDto;
import com.intive.patronage22.szczecin.retroboard.exception.BoardNotFoundException;
import com.intive.patronage22.szczecin.retroboard.exception.UserIsNotOwnerException;
import com.intive.patronage22.szczecin.retroboard.model.Board;
import com.intive.patronage22.szczecin.retroboard.model.User;
import com.intive.patronage22.szczecin.retroboard.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BoardService {
    private final BoardRepository boardRepository;
    private final UserService userService;

    @Transactional(readOnly = true)
    public List<BoardDto> getUserBoards(final String uid) {
        if (uid == null || uid.isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid request - no uid given!");

        final User user = userService.findUserById(uid);

        return user.getUserBoards().stream()
                .map(BoardDto::fromModel)
                .collect(Collectors.toList());
    }

    @Transactional
    public BoardDto createNewBoard(final String boardName, final String uid) {

        final User user = userService.findUserById(uid);
        final Board newBoard = Board.builder()
                .name(boardName)
                .state(EnumStateDto.CREATED)
                .creator(user)
                .users(Set.of())
                .build();

        return BoardDto.fromModel(boardRepository.save(newBoard));
    }

    @Transactional
    public void delete(final int boardId, final String uid) {

        final User user = userService.findUserById(uid);

        final Board board = boardRepository.findById(boardId)
                .orElseThrow(BoardNotFoundException::new);

        if(!(user.equals(board.getCreator()))){
            throw new UserIsNotOwnerException();
        }else{
            boardRepository.deleteById(boardId);
        }
    }
}
