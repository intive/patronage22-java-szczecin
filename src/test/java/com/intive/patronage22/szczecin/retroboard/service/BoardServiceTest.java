package com.intive.patronage22.szczecin.retroboard.service;

import com.google.firebase.auth.FirebaseAuthException;
import com.intive.patronage22.szczecin.retroboard.dto.BoardDto;
import com.intive.patronage22.szczecin.retroboard.dto.EnumStateDto;
import com.intive.patronage22.szczecin.retroboard.exception.BoardNotFoundException;
import com.intive.patronage22.szczecin.retroboard.exception.UserIsNotOwnerException;
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
@SpringBootTest
class BoardServiceTest {

    @Autowired
    private BoardService boardService;


    @Autowired
    private UserService userService;

    @MockBean
    UserRepository userRepository;

    @MockBean
    BoardRepository boardRepository;

    @Test
    void createNewBoardShouldReturnBoardDtoWhenUserExist() {
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
    void deleteBoardShouldReturnNotFoundWhenUserNotExists(){

        //given
        final String uid = "uid101";
        final int bid = 101;

        //when
        when(userRepository.findById(uid)).thenReturn(Optional.empty());

        //then
        assertThrows(UserNotFoundException.class,
                () -> boardService.delete(bid,uid));
    }

    @Test
    void deleteBoardShouldReturnForbiddenWhenUserIsNotOwner(){

        //given
        final String uid = "uid101";
        final int bid = 101;

        final User user = new User(uid, "Josef", Set.of());
        final Board board = Board.builder()
                .id(bid)
                .name("boardName")
                .state(EnumStateDto.CREATED)
                .creator(user)
                .users(Set.of())
                .build();


        //when
        when(userRepository.findById(uid)).thenReturn(Optional.of(board.getCreator()));

        //then
        assertThrows(UserIsNotOwnerException.class,
                () -> boardService.delete(bid,uid));
    }

    @Test
    void deleteBoardShouldReturnNotFoundWhenBoardNotExist(){

        //given
        final String uid = "uid101";
        final int bid = 101;

        //when
        when(boardRepository.findById(bid)).thenReturn(Optional.empty());

        //then
        assertThrows(BoardNotFoundException.class,
                () -> boardService.delete(bid,uid));
    }
}