package com.intive.patronage22.szczecin.retroboard.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intive.patronage22.szczecin.retroboard.configuration.security.SecurityConfig;
import com.intive.patronage22.szczecin.retroboard.dto.BoardCardDataDto;
import com.intive.patronage22.szczecin.retroboard.dto.BoardCardsColumn;
import com.intive.patronage22.szczecin.retroboard.dto.BoardDataDto;
import com.intive.patronage22.szczecin.retroboard.dto.BoardDto;
import com.intive.patronage22.szczecin.retroboard.dto.EnumStateDto;
import com.intive.patronage22.szczecin.retroboard.exception.UserNotFoundException;
import com.intive.patronage22.szczecin.retroboard.service.BoardService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest({BoardController.class, SecurityConfig.class})
class BoardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BoardService boardService;

    @MockBean
    private InMemoryUserDetailsManager inMemoryUserDetailsManager;

    @Test
    void getUserBoardsShouldReturnOkWhenUserExist() throws Exception {
        // given
        final String url = "/boards";
        final String uid = "uid101";

        final List<BoardDto> dtoList = List.of(
                new BoardDto(1, EnumStateDto.CREATED, "test1"),
                new BoardDto(2, EnumStateDto.CREATED, "test2")
        );

        // when
        when(boardService.getUserBoards(uid)).thenReturn(dtoList);

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
        when(boardService.getUserBoards(uid)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

        // then
        mockMvc.perform(get(url).param("userId", uid))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ResponseStatusException));
    }

    @Test
    void getUserBoardsShouldReturnBadRequestWhenNoUserGiven() throws Exception {
        // given
        final String url = "/boards?userId=";

        // when
        when(boardService.getUserBoards(anyString())).thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST));

        // then
        mockMvc.perform(get(url))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ResponseStatusException));
    }

    @Test
    void createNewBoardShouldReturnCreatedWhenUserExist() throws Exception {
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
        when(boardService.createNewBoard(boardName, uid)).thenReturn(boardDto);

        // then
        mockMvc.perform(post(url).param("userId", uid)
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
        when(boardService.createNewBoard(boardName, uid)).thenThrow(new UserNotFoundException());

        // then
        mockMvc.perform(post(url).param("userId", uid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + boardName + "\"}")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("getBoardDataById should return 200 when board data is collected")
    void getBoardDataByIdWhenBoardDataIsCollectedThenShouldReturnOk() throws Exception {
        //given
        final String loginUrl = "/login";
        final String username  = "testuser@example.com";
        final String password = "1234";
        final String encodedPassword = "$2a$10$A0IKJqSv.cSqXb7BuIPw4.GvP1U3VPUIRvkAigPVr6HipH.R3nGLO";
        final String boardDataUrl = "/boards";
        final int boardId = 1;

        final BoardDto boardDto = new BoardDto(1, EnumStateDto.CREATED, "test1");
        final List<String> actionTexts = List.of("action text 1", "action text 2");
        final BoardCardDataDto boardCardDataDto =
                new BoardCardDataDto(2, "cardText", BoardCardsColumn.SUCCESS, username, actionTexts);
        final BoardCardDataDto boardCardDataDto1 =
                new BoardCardDataDto(3, "cardText3", BoardCardsColumn.FAILURES, username, actionTexts);
        final BoardDataDto boardDataDto = new BoardDataDto(boardDto, List.of(boardCardDataDto, boardCardDataDto1));

        //when
        when(boardService.getBoardDataById(boardId, username)).thenReturn(boardDataDto);
        when(inMemoryUserDetailsManager.loadUserByUsername(username)).thenReturn(
                createExistingUser(username, encodedPassword, "user"));

        //then
        final String token = createToken(loginUrl, username, password);
        this.mockMvc.perform(get(boardDataUrl + "/" + boardId).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.*", hasSize(2))).andExpect(result -> assertTrue(
                        result.getResponse().getContentAsString().contains(boardDataDto.getBoard().getId().toString())))
                .andExpect(result -> assertTrue(
                        result.getResponse().getContentAsString().contains(boardDataDto.getBoard().getName())))
                .andExpect(result -> assertTrue(result.getResponse().getContentAsString()
                        .contains(boardDataDto.getBoardCards().get(0).getId().toString()))).andExpect(
                        result -> assertTrue(result.getResponse().getContentAsString()
                                .contains(boardDataDto.getBoardCards().get(0).getCardText()))).andExpect(result -> assertTrue(
                        result.getResponse().getContentAsString()
                                .contains(boardDataDto.getBoardCards().get(0).getColumnName().toString()))).andExpect(
                        result -> assertTrue(result.getResponse().getContentAsString()
                                .contains(boardDataDto.getBoardCards().get(1).getBoardCardCreator()))).andExpect(
                        result -> assertTrue(result.getResponse().getContentAsString()
                                .contains(boardDataDto.getBoardCards().get(1).getId().toString()))).andExpect(
                        result -> assertTrue(result.getResponse().getContentAsString()
                                .contains(boardDataDto.getBoardCards().get(1).getActionTexts().get(1))));
    }

    @Test
    @DisplayName("getBoardDataById should return 400 when user has no permission to view board data")
    void getBoardDataByIdWhenUserHasNoPermissionToViewBoardDataThenShouldThrowBadRequestException() throws Exception {
        //given
        final String loginUrl = "/login";
        final String username = "testuser@example.com";
        final String password = "1234";
        final String encodedPassword = "$2a$10$A0IKJqSv.cSqXb7BuIPw4.GvP1U3VPUIRvkAigPVr6HipH.R3nGLO";
        final String boardDataUrl = "/boards";
        final int boardId = 1;
        final String exceptionMessage = "User has no permission to view board data.";

        //when
        when(boardService.getBoardDataById(boardId, username)).thenThrow(
                new ResponseStatusException(HttpStatus.BAD_REQUEST, exceptionMessage));
        when(inMemoryUserDetailsManager.loadUserByUsername(username)).thenReturn(
                createExistingUser(username, encodedPassword, "user"));

        //then
        final String token = createToken(loginUrl, username, password);
        this.mockMvc.perform(get(boardDataUrl + "/" + boardId).header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ResponseStatusException))
                .andExpect(result -> assertTrue(result.getResolvedException().getMessage().contains(exceptionMessage)));
    }

    @Test
    @DisplayName("getBoardDataById should return 404 when board does not exist")
    void getBoardDataByIdWhenBoardNotFoundThenShouldThrowNotFoundException() throws Exception {
        //given
        final String loginUrl = "/login";
        final String username = "testuser@example.com";
        final String password = "1234";
        final String encodedPassword = "$2a$10$A0IKJqSv.cSqXb7BuIPw4.GvP1U3VPUIRvkAigPVr6HipH.R3nGLO";
        final String boardDataUrl = "/boards";
        final int boardId = 1;
        final String exceptionMessage = "Board is not found.";

        //when
        when(boardService.getBoardDataById(boardId, username)).thenThrow(
                new ResponseStatusException(HttpStatus.NOT_FOUND, exceptionMessage));
        when(inMemoryUserDetailsManager.loadUserByUsername(username)).thenReturn(
                createExistingUser(username, encodedPassword, "user"));

        //then
        final String token = createToken(loginUrl, username, password);
        this.mockMvc.perform(get(boardDataUrl + "/" + boardId).header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ResponseStatusException))
                .andExpect(result -> assertTrue(result.getResolvedException().getMessage().contains(exceptionMessage)));
    }

    private UserDetails createExistingUser(final String username, final String encodedPassword, final String role) {
        return User.withUsername(username).password(encodedPassword).roles(role.toUpperCase(Locale.ROOT)).build();
    }

    private String createToken(final String loginUrl, final String username, final String password) throws Exception {
        final String loginJsonResult = this.mockMvc.perform(
                        post(loginUrl).param("username", username).param("password", password)
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED)).andReturn().getResponse()
                .getContentAsString();
        final TypeReference<Map<String, String>> tr = new TypeReference<>() {};
        final Map<String, String> map = objectMapper.readValue(loginJsonResult, tr);
        return map.get("access_token");
    }

}
