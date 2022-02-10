package com.intive.patronage22.szczecin.retroboard.controller;

import com.intive.patronage22.szczecin.retroboard.configuration.FirebaseTestConfiguration;
import com.intive.patronage22.szczecin.retroboard.dto.BoardDto;
import com.intive.patronage22.szczecin.retroboard.dto.EnumStateDto;
import com.intive.patronage22.szczecin.retroboard.model.Board;
import com.intive.patronage22.szczecin.retroboard.model.User;
import com.intive.patronage22.szczecin.retroboard.repository.BoardRepository;
import com.intive.patronage22.szczecin.retroboard.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {FirebaseTestConfiguration.class})
class BoardControllerTest {
    @Autowired
    private BoardController boardController;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BoardRepository boardRepository;

    @Test
    public void whenUserDoesNotExist_thenStatusNotFound() {
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            boardController.getUserBoards("xyz");
        });
        assertTrue(exception.getStatus() == HttpStatus.NOT_FOUND);
    }

    @Test
    public void whenUserExists_thenStatusOk() {
        User user = createUser("abc", null);
        userRepository.save(user);
        ResponseEntity<List<BoardDto>> res = boardController.getUserBoards("abc");
        assertTrue(res.getStatusCode() == HttpStatus.OK);
        assertTrue(res.hasBody());
    }

    @Test
    public void whenUserHasTwoBoards_thenBodyShouldContainTwoBoards() {
        User user1 = createUser("def","Test user 1");
        userRepository.save(user1);

        Board board1 = createBoard("Test board 1", user1);
        boardRepository.save(board1);

        Board board2 = createBoard("Test board 2", user1);
        boardRepository.save(board2);

        User user2 = createUser("ghi","Test user 2");
        userRepository.save(user2);

        User user3 = createUser("jkl","Test user 3");
        userRepository.save(user3);

        board1.getUsers().add(user2);
        board1.getUsers().add(user3);
        boardRepository.save(board1);

        board2.getUsers().add(user2);
        boardRepository.save(board2);
        
        ResponseEntity<List<BoardDto>> res = boardController.getUserBoards(user2.getUid());
        assertTrue(res.getStatusCode() == HttpStatus.OK);
        assertTrue(res.getBody().size() == 2);
    }

    private User createUser(String uid, String name) {
        return new User(uid, name, new HashSet<>());
    }

    private Board createBoard(String name, User creator) {
        return new Board(null, name, EnumStateDto.CREATED, creator, new HashSet<>());
    }
}
