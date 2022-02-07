package com.intive.patronage22.szczecin.retroboard.controller;

import com.intive.patronage22.szczecin.retroboard.dto.BoardDto;
import com.intive.patronage22.szczecin.retroboard.model.Board;
import com.intive.patronage22.szczecin.retroboard.model.User;
import com.intive.patronage22.szczecin.retroboard.repository.BoardRepository;
import com.intive.patronage22.szczecin.retroboard.repository.UserRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest
public class BoardControllerTest {
    @Autowired
    BoardController boardController;

    @Autowired
    UserRepository userRepository;

    @Autowired
    BoardRepository boardRepository;

    @Test
    public void contextLoads() {
    }

    @Test(expected = ResponseStatusException.class)
    public void whenUserDoesNotExist_thenStatusNotFound() {
        ResponseEntity<List<BoardDto>> res = boardController.getUserBoards("xyz");
        assertTrue(res.getStatusCode() == HttpStatus.NOT_FOUND);
    }

    @Test
    public void whenUserExists_thenStatusOk() {
        User user = new User();
        user.setUid("abc");
        long userCount = userRepository.count();
        userRepository.save(user);
        assertTrue(userRepository.count() == userCount + 1);
        ResponseEntity<List<BoardDto>> res = boardController.getUserBoards("abc");
        userRepository.delete(user);
        assertTrue(res.getStatusCode() == HttpStatus.OK);
        assertTrue(res.hasBody());
    }

    @Test
    public void whenUserHasTwoBoards_thenBodyShouldContainTwoBoards() {
        User user1 = new User();
        user1.setUid("def");
        user1.setName("Test user 1");
        userRepository.save(user1);

        Board board1 = new Board();
        board1.setName("Test board 1");
        board1.setCreator(user1);
        boardRepository.save(board1);

        Board board2 = new Board();
        board2.setName("Test board 2");
        board2.setCreator(user1);
        boardRepository.save(board2);

        User user2 = new User();
        user2.setUid("ghi");
        user2.setName("Test user 2");
        userRepository.save(user2);
        
        User user3 = new User();
        user3.setUid("jkl");
        user3.setName("Test user 3");
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
}
