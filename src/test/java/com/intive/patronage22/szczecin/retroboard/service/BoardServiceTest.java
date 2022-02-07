package com.intive.patronage22.szczecin.retroboard.service;

import com.intive.patronage22.szczecin.retroboard.dto.BoardDto;
import com.intive.patronage22.szczecin.retroboard.dto.EnumStateDto;
import com.intive.patronage22.szczecin.retroboard.exception.UserNotFoundException;
import com.intive.patronage22.szczecin.retroboard.model.Board;
import com.intive.patronage22.szczecin.retroboard.model.User;
import com.intive.patronage22.szczecin.retroboard.repository.BoardRepository;
import com.intive.patronage22.szczecin.retroboard.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Collections;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
class BoardServiceTest {

    @Mock UserRepository userRepository;
    @Mock BoardRepository boardRepository;

    private BoardService boardService;

    @BeforeEach
    void setUp() {
        boardService = new BoardService(boardRepository, userRepository);
    }

    @ParameterizedTest
    @MethodSource("validBoardData")
    void saveBoardForUserId_shouldNotThrow(String boardName, String userId) {
        // given
        Optional<User> optionalUser = Optional.of(new User(userId, "Josef"));
        Board board = new Board(10, boardName, EnumStateDto.CREATED,
                optionalUser.get(), Collections.emptySet());

        when(userRepository.findById(userId)).thenReturn(optionalUser);
        when(boardRepository.save(new Board(boardName, any()))).thenReturn(board);

        BoardDto boardDtoExpected = BoardDto.mapToDto(board);

        // when
        BoardDto boardDtoResult =
                boardService.saveBoardForUserId(boardName, userId);

        // then
        assertEquals(boardDtoExpected, boardDtoResult);
        verify(userRepository).findById(anyString());
        verify(boardRepository).save(any(Board.class));
    }

    @ParameterizedTest
    @MethodSource("validBoardData")
    void saveBoardForUserId_shouldThrowUserNotFoundException(String boardName,
                                                             String userId) {
        // given
        when(userRepository.findById(anyString())).thenReturn(Optional.empty());

        // then
        assertThrows(UserNotFoundException.class,
                () -> boardService.saveBoardForUserId(boardName, userId));
    }

    private static Stream<Arguments> validBoardData() {
        return Stream.of(Arguments.of("test1", "uid10"));
    }
}