package com.intive.patronage22.szczecin.retroboard.controller;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.intive.patronage22.szczecin.retroboard.configuration.security.SecurityConfig;
import com.intive.patronage22.szczecin.retroboard.dto.BoardCardDto;
import com.intive.patronage22.szczecin.retroboard.dto.BoardCardsColumn;
import com.intive.patronage22.szczecin.retroboard.dto.BoardDataDto;
import com.intive.patronage22.szczecin.retroboard.dto.BoardDto;
import com.intive.patronage22.szczecin.retroboard.dto.EnumStateDto;
import com.intive.patronage22.szczecin.retroboard.exception.BadRequestException;
import com.intive.patronage22.szczecin.retroboard.exception.NotFoundException;
import com.intive.patronage22.szczecin.retroboard.service.BoardService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
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

    @MockBean
    private BoardService boardService;

    @MockBean
    private FirebaseAuth firebaseAuth;

    @Test
    void getUserBoardsShouldReturnOkWhenUserExist() throws Exception {
        // given
        final String url = "/boards";
        final String uid = "uid101";

        final List<BoardDto> boardList = List.of(
                new BoardDto(1, EnumStateDto.CREATED, "test1"),
                new BoardDto(2, EnumStateDto.CREATED, "test2")
        );

        // when
        when(boardService.getUserBoards(uid)).thenReturn(boardList);

        // then
        mockMvc.perform(get(url)
                        .param("userId", uid))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.*", hasSize(2)));
    }

    @Test
    void getUserBoardsShouldReturnBadRequestWhenUserDoesNotExist() throws Exception {
        // given
        final String url = "/boards";
        final String uid = "uid101";

        // when
        when(boardService.getUserBoards(uid)).thenThrow(BadRequestException.class);

        // then
        mockMvc.perform(get(url).param("userId", uid))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof BadRequestException));
    }

    @Test
    void getUserBoardsShouldReturnBadRequestWhenNoUserGiven() throws Exception {
        // given
        final String url = "/boards?userId=";

        // when
        when(boardService.getUserBoards(anyString())).thenThrow(BadRequestException.class);

        // then
        mockMvc.perform(get(url))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof BadRequestException));
    }

    @Test
    void createBoardShouldReturnCreatedWhenUserExistsAndBoardNameIsValid() throws Exception {
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
        when(boardService.createBoard(boardName, uid)).thenReturn(boardDto);

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
    void createBoardShouldReturnNotFoundWhenUserNotExist() throws Exception {
        // given
        final String url = "/boards";
        final String uid = "uid101";
        final String boardName = "My first board.";

        // when
        when(boardService.createBoard(boardName, uid)).thenThrow(new NotFoundException("User not found"));

        // then
        mockMvc.perform(post(url).param("userId", uid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + boardName + "\"}")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("getBoardDataById should return 200 when board data is collected")
    void getBoardDataByIdShouldReturnOk() throws Exception {
        //given
        final String email = "test22@test.com";
        final String providedAccessToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9" +
                                   ".eyJzdWIiOiJzb21ldXNlciIsInJvbGVzIjpbIlJPTEVfVVNFUiJdLCJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjgwODAvbG9naW4ifQ.vDeQLA7Y8zTXaJW8bF08lkWzzwGi9Ll44HeMbOc22_o";
        final String boardDataUrl = "/boards";
        final int boardId = 1;

        final BoardDto boardDto = new BoardDto(1, EnumStateDto.CREATED, "test1");
        final List<String> actionTexts = List.of("action text 1", "action text 2");
        final BoardCardDto boardCardDataDto =
                new BoardCardDto(2, "cardText", BoardCardsColumn.SUCCESS, email, actionTexts);
        final BoardCardDto boardCardDataDto1 =
                new BoardCardDto(3, "cardText3", BoardCardsColumn.FAILURES, email, actionTexts);
        final BoardDataDto boardDataDto = new BoardDataDto(boardDto, List.of(boardCardDataDto, boardCardDataDto1));

        final FirebaseToken firebaseToken = mock(FirebaseToken.class);

        //when
        when(firebaseToken.getEmail()).thenReturn(email);
        when(firebaseAuth.verifyIdToken(providedAccessToken)).thenReturn(firebaseToken);

        when(boardService.getBoardDataById(boardId, email)).thenReturn(boardDataDto);

        //then
        this.mockMvc.perform(get(boardDataUrl + "/" + boardId)
                        .header("Authorization", "Bearer " + providedAccessToken))
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
    void getBoardDataByIdShouldThrowBadRequestWhenUserDoesntHavePermissions() throws Exception {
        //given
        final String email = "test22@test.com";
        final String providedAccessToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9" +
                                   ".eyJzdWIiOiJzb21ldXNlciIsInJvbGVzIjpbIlJPTEVfVVNFUiJdLCJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjgwODAvbG9naW4ifQ.vDeQLA7Y8zTXaJW8bF08lkWzzwGi9Ll44HeMbOc22_o";
        final String boardDataUrl = "/boards";
        final int boardId = 1;
        final String exceptionMessage = "User doesn't have permissions to view board data.";

        final FirebaseToken firebaseToken = mock(FirebaseToken.class);

        //when
        when(firebaseToken.getEmail()).thenReturn(email);
        when(firebaseAuth.verifyIdToken(providedAccessToken)).thenReturn(firebaseToken);

        when(boardService.getBoardDataById(boardId, email)).thenThrow(new BadRequestException(exceptionMessage));

        //then
        this.mockMvc.perform(get(boardDataUrl + "/" + boardId).header("Authorization", "Bearer " + providedAccessToken))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof BadRequestException))
                .andExpect(result -> assertTrue(result.getResolvedException().getMessage().contains(exceptionMessage)));
    }

    @Test
    @DisplayName("getBoardDataById should return 404 when board does not exist")
    void getBoardDataByIdShouldThrowNotFoundWhenBoardDoesNotExist() throws Exception {
        //given
        final String email = "test22@test.com";
        final String providedAccessToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9" +
                                   ".eyJzdWIiOiJzb21ldXNlciIsInJvbGVzIjpbIlJPTEVfVVNFUiJdLCJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjgwODAvbG9naW4ifQ.vDeQLA7Y8zTXaJW8bF08lkWzzwGi9Ll44HeMbOc22_o";
        final String boardDataUrl = "/boards";
        final int boardId = 1;
        final String exceptionMessage = "Board is not found.";

        final FirebaseToken firebaseToken = mock(FirebaseToken.class);
        // when
        when(firebaseToken.getEmail()).thenReturn(email);
        when(firebaseAuth.verifyIdToken(providedAccessToken)).thenReturn(firebaseToken);

        when(boardService.getBoardDataById(boardId, email)).thenThrow(new NotFoundException(exceptionMessage));

        //then
        this.mockMvc.perform(get(boardDataUrl + "/" + boardId)
                        .header(AUTHORIZATION, "Bearer " + providedAccessToken))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof NotFoundException))
                .andExpect(result -> assertTrue(result.getResolvedException().getMessage().contains(exceptionMessage)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "  ", "123",
            "01234567890123456789012345678901234567890123456789012345678912345"})
    void createNewBoardShouldReturnBadRequestWhenBoardNameIsNotValid(final String boardName) throws Exception {
        // given
        final String url = "/boards";
        final String uid = "uid101";

        // then
        final MvcResult result = mockMvc
                .perform(post(url)
                        .param("userId", uid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + boardName + "\"}")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest()).andReturn();

        final Exception resultException = result.getResolvedException();

        assertInstanceOf(MethodArgumentNotValidException.class, resultException);
    }

    @Test
    void createNewBoardShouldReturnBadRequestWhenBoardNameIsNull() throws Exception {
        // given
        final String url = "/boards";
        final String uid = "uid101";
        final String boardName = null;

        // then
        final MvcResult result = mockMvc
                .perform(post(url)
                        .param("userId", uid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":" + boardName + "}")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest()).andReturn();

        final Exception resultException = result.getResolvedException();

        assertInstanceOf(MethodArgumentNotValidException.class, resultException);
    }
}
