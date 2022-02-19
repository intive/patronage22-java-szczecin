package com.intive.patronage22.szczecin.retroboard.service;

import com.intive.patronage22.szczecin.retroboard.dto.BoardDto;
import com.intive.patronage22.szczecin.retroboard.dto.EnumStateDto;
import com.intive.patronage22.szczecin.retroboard.exception.BoardNameFormatException;
import com.intive.patronage22.szczecin.retroboard.exception.UserNotFoundException;
import com.intive.patronage22.szczecin.retroboard.model.Board;
import com.intive.patronage22.szczecin.retroboard.model.User;
import com.intive.patronage22.szczecin.retroboard.repository.BoardRepository;
import com.intive.patronage22.szczecin.retroboard.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = BoardService.class)
class BoardServiceTest {

    @Autowired
    private BoardService boardService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private BoardRepository boardRepository;

    @Test
    void createNewBoardShouldReturnBoardDtoWhenUserExistsAndBoardNameIsNotEmpty() {
        // given
        final String uid = "uid101";
        final String boardName = "My first board.";

        final User user = new User(uid, "Josef", Set.of());

        final Board board = Board.builder()
                .id(10)
                .name(boardName)
                .state(EnumStateDto.CREATED)
                .creator(user)
                .users(Set.of())
                .build();

        // when
        when(userRepository.findById(uid)).thenReturn(Optional.of(user));
        when(boardRepository.save(any(Board.class))).thenReturn(board);
        final BoardDto boardDtoResult = boardService.createNewBoard(boardName, uid);

        // then
        assertEquals(BoardDto.fromModel(board), boardDtoResult);
        verify(userRepository).findById(uid);
        verify(boardRepository).save(any(Board.class));
    }

    @Test
    void createNewBoardShouldReturnNoFoundWhenUserNotExist() {
        // given
        final String uid = "uid101";
        final String boardName = "My first board.";

        // when
        when(userRepository.findById(uid)).thenReturn(Optional.empty());

        // then
        assertThrows(UserNotFoundException.class,
                () -> boardService.createNewBoard(boardName, uid));
    }

    @Test
    void createNewBoardShouldReturnBoardNameFormatExceptionWhenBoardNameIsEmpty() {
        // given
        final String uid = "uid101";
        final String boardName = "";

        // then
        assertThrows(BoardNameFormatException.class,
                () -> boardService.createNewBoard(boardName, uid));
    }
}