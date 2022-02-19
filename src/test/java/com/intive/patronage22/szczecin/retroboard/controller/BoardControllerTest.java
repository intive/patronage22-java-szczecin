package com.intive.patronage22.szczecin.retroboard.controller;

import com.intive.patronage22.szczecin.retroboard.configuration.security.SecurityConfig;
import com.intive.patronage22.szczecin.retroboard.dto.BoardDto;
import com.intive.patronage22.szczecin.retroboard.dto.EnumStateDto;
import com.intive.patronage22.szczecin.retroboard.exception.BoardNameFormatException;
import com.intive.patronage22.szczecin.retroboard.exception.UserNotFoundException;
import com.intive.patronage22.szczecin.retroboard.service.BoardService;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@WebMvcTest({BoardController.class, SecurityConfig.class})
class BoardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BoardService boardService;

    @Test
    void getUserBoardsShouldReturnOkWhenUserExist() throws Exception{
        // given
        final String url = "/boards";
        final String uid = "uid101";

        final List<BoardDto> dtoList = List.of(
                new BoardDto(1, EnumStateDto.CREATED, "test1"),
                new BoardDto(2, EnumStateDto.CREATED, "test2")
        );

        // when
        when(boardService.getUserBoards(uid))
                .thenReturn(dtoList);

        // then
        mockMvc.perform(get(url)
                        .param("userId", uid))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.*", hasSize(2)));
    }

    @Test
    void getUserBoardsShouldReturnNotFoundWhenUserNotExist() throws Exception {
        // given
        final String url = "/boards";
        final String uid = "uid101";

        // when
        when(boardService.getUserBoards(uid))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

        // then
        mockMvc.perform(get(url)
                        .param("userId", uid))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException()
                        instanceof ResponseStatusException));
    }

    @Test
    void getUserBoardsShouldReturnBadRequestWhenNoUserGiven() throws Exception {
        // given
        final String url = "/boards?userId=";

        // when
        when(boardService.getUserBoards(anyString()))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST));

        // then
        mockMvc.perform(get(url))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException()
                        instanceof ResponseStatusException));
    }

    @Test
    void createNewBoardShouldReturnCreatedWhenUserExistsAndBoardNameIsNotEmpty() throws Exception {
        // given
        final String url = "/boards";
        final String uid = "uid101";
        final String boardName = "My first board.";

        final BoardDto boardDto = BoardDto.builder()
                .id(1004)
                .state(EnumStateDto.CREATED)
                .name(boardName)
                .build();

        // when
        when(boardService.createNewBoard(boardName, uid))
                .thenReturn(boardDto);

        // then
        mockMvc.perform(post(url)
                        .param("userId", uid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + boardName + "\"}")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(boardName));
    }

    @Test
    void createNewBoardShouldReturnNotFoundWhenUserNotExist() throws Exception {
        // given
        final String url = "/boards";
        final String uid = "uid101";
        final String boardName = "My first board.";

        // when
        when(boardService.createNewBoard(boardName, uid))
                .thenThrow(new UserNotFoundException());

        // then
        mockMvc.perform(post(url)
                        .param("userId", uid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + boardName + "\"}")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void createNewBoardShouldReturnBardNameFormatExceptionWhenBoardNameIsEmpty() throws Exception {
        // given
        final String url = "/boards";
        final String uid = "uid101";
        final String boardName = "";

        // when
        when(boardService.createNewBoard(boardName, uid))
                .thenThrow(new BoardNameFormatException());

        // then
        mockMvc.perform(post(url)
                        .param("userId", uid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + boardName + "\"}")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}