package com.intive.patronage22.szczecin.retroboard.controller;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.intive.patronage22.szczecin.retroboard.configuration.security.SecurityConfig;
import com.intive.patronage22.szczecin.retroboard.dto.BoardCardDto;
import com.intive.patronage22.szczecin.retroboard.dto.BoardCardsColumn;
import com.intive.patronage22.szczecin.retroboard.dto.BoardCardsColumnDto;
import com.intive.patronage22.szczecin.retroboard.dto.BoardDataDto;
import com.intive.patronage22.szczecin.retroboard.dto.BoardDetailsDto;
import com.intive.patronage22.szczecin.retroboard.dto.BoardDto;
import com.intive.patronage22.szczecin.retroboard.dto.BoardPatchDto;
import com.intive.patronage22.szczecin.retroboard.dto.EnumStateDto;
import com.intive.patronage22.szczecin.retroboard.dto.UserDto;
import com.intive.patronage22.szczecin.retroboard.exception.BadRequestException;
import com.intive.patronage22.szczecin.retroboard.exception.NotFoundException;
import com.intive.patronage22.szczecin.retroboard.model.User;
import com.intive.patronage22.szczecin.retroboard.repository.UserRepository;
import com.intive.patronage22.szczecin.retroboard.service.BoardService;
import org.json.JSONArray;
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
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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

    @MockBean
    private UserRepository userRepository;

    private static final String email = "test22@test.com";
    private static final String providedAccessToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9" +
                                       ".eyJzdWIiOiJzb21ldXNlciIsInJvbGVzIjpbIlJPTEVfVVNFUiJdLCJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjgwODAvbG9naW4ifQ.vDeQLA7Y8zTXaJW8bF08lkWzzwGi9Ll44HeMbOc22_o";
    private static final String boardDataUrl = "/api/v1/boards";

    @Test
    void getUserBoardsShouldReturnOkWhenUserExist() throws Exception {
        // given
        final List<BoardDto> dtoList = List.of(
                new BoardDto(1, EnumStateDto.CREATED, "test1", 1),
                new BoardDto(2, EnumStateDto.CREATED, "test2", 2)
        );

        final FirebaseToken firebaseToken = mock(FirebaseToken.class);

        // when
        when(firebaseToken.getEmail()).thenReturn(email);
        when(firebaseAuth.verifyIdToken(providedAccessToken)).thenReturn(firebaseToken);
        when(boardService.getUserBoards(email)).thenReturn(dtoList);

        // then
        mockMvc.perform(get(boardDataUrl)
                        .header(AUTHORIZATION, "Bearer " + providedAccessToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.*", hasSize(2)));
    }

    @Test
    void getUserBoardsShouldReturnBadRequestWhenUserDoesNotExist() throws Exception {
        // given
        final FirebaseToken firebaseToken = mock(FirebaseToken.class);

        // when
        when(firebaseToken.getEmail()).thenReturn(email);
        when(firebaseAuth.verifyIdToken(providedAccessToken)).thenReturn(firebaseToken);
        when(boardService.getUserBoards(email)).thenThrow(BadRequestException.class);

        // then
        mockMvc.perform(get(boardDataUrl)
                        .header(AUTHORIZATION, "Bearer " + providedAccessToken))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof BadRequestException));
    }

    @Test
    void createBoardShouldReturnCreatedWhenUserExistsAndBoardNameIsValid() throws Exception {
        // given
        final String boardName = "My first board.";

        final BoardDto boardDto = BoardDto.builder()
                .id(1004)
                .state(EnumStateDto.CREATED)
                .name(boardName)
                .build();

        final FirebaseToken firebaseToken = mock(FirebaseToken.class);

        // when
        when(firebaseToken.getEmail()).thenReturn(email);
        when(firebaseAuth.verifyIdToken(providedAccessToken)).thenReturn(firebaseToken);
        when(boardService.createBoard(boardName, email)).thenReturn(boardDto);

        // then
        mockMvc.perform(post(boardDataUrl)
                        .header(AUTHORIZATION, "Bearer " + providedAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + boardName + "\"}")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(boardName));
    }

    @Test
    void createBoardShouldReturnBadRequestWhenUserDoesNotExist() throws Exception {
        // given
        final String boardName = "My first board.";

        final FirebaseToken firebaseToken = mock(FirebaseToken.class);

        // when
        when(firebaseToken.getEmail()).thenReturn(email);
        when(firebaseAuth.verifyIdToken(providedAccessToken)).thenReturn(firebaseToken);
        when(boardService.createBoard(boardName, email)).thenThrow(new BadRequestException("User not found"));

        // then
        mockMvc.perform(post(boardDataUrl)
                        .header(AUTHORIZATION, "Bearer " + providedAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + boardName + "\"}")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("getBoardDataById should return 200 when board data is collected")
    void getBoardDataByIdShouldReturnOk() throws Exception {
        //given
        final int boardId = 1;

        final List<BoardCardsColumnDto> boardCardsColumnDtos =
                List.of(BoardCardsColumnDto.createFrom(BoardCardsColumn.SUCCESS),
                        BoardCardsColumnDto.createFrom(BoardCardsColumn.FAILURES),
                        BoardCardsColumnDto.createFrom(BoardCardsColumn.KUDOS));
        final List<UserDto> userDtos =
                List.of(new UserDto("test@example.com", "uid123"), new UserDto("test1@example.com", "uid1235"));
        final BoardDto boardDto = new BoardDto(1, EnumStateDto.CREATED, "board name", 5);
        final BoardDataDto boardDataDto = new BoardDataDto(boardDto, boardCardsColumnDtos, userDtos);

        final FirebaseToken firebaseToken = mock(FirebaseToken.class);

        //when
        when(firebaseToken.getEmail()).thenReturn(email);
        when(firebaseAuth.verifyIdToken(providedAccessToken)).thenReturn(firebaseToken);

        when(boardService.getBoardDataById(boardId, email)).thenReturn(boardDataDto);

        //then
        this.mockMvc.perform(get(boardDataUrl + "/" + boardId).header(AUTHORIZATION, "Bearer " + providedAccessToken))
                .andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.*", hasSize(3)))
                .andExpect(result -> assertTrue(result.getResponse().getContentAsString()
                        .contains(boardDataDto.getBoard().getId().toString())))
                .andExpect(result -> assertTrue(result.getResponse().getContentAsString()
                        .contains(boardDataDto.getBoard().getName())))
                .andExpect(result -> assertTrue(result.getResponse().getContentAsString()
                        .contains(String.valueOf(boardDataDto.getBoard().getNumberOfVotes()))))
                .andExpect(result -> assertTrue(result.getResponse().getContentAsString()
                        .contains(boardDataDto.getColumns().get(0).getName())))
                .andExpect(result -> assertTrue(result.getResponse().getContentAsString()
                        .contains(boardDataDto.getColumns().get(1).getId().toString())))
                .andExpect(result -> assertTrue(result.getResponse().getContentAsString()
                        .contains(boardDataDto.getColumns().get(2).getColour())))
                .andExpect(result -> assertTrue(result.getResponse().getContentAsString()
                        .contains(boardDataDto.getUsers().get(0).getEmail())))
                .andExpect(result -> assertTrue(result.getResponse().getContentAsString()
                        .contains(boardDataDto.getUsers().get(1).getId())));
    }

    @Test
    @DisplayName("getBoardDataById should return 400 when user has no permission to view board data")
    void getBoardDataByIdShouldThrowBadRequestWhenUserDoesntHavePermissions() throws Exception {
        //given
        final int boardId = 1;
        final String exceptionMessage = "User doesn't have permissions to view board data.";

        final FirebaseToken firebaseToken = mock(FirebaseToken.class);

        //when
        when(firebaseToken.getEmail()).thenReturn(email);
        when(firebaseAuth.verifyIdToken(providedAccessToken)).thenReturn(firebaseToken);

        when(boardService.getBoardDataById(boardId, email)).thenThrow(new BadRequestException(exceptionMessage));

        //then
        this.mockMvc.perform(get(boardDataUrl + "/" + boardId).header(AUTHORIZATION, "Bearer " + providedAccessToken))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof BadRequestException))
                .andExpect(result -> assertTrue(result.getResolvedException().getMessage().contains(exceptionMessage)));
    }

    @Test
    @DisplayName("getBoardDataById should return 404 when board does not exist")
    void getBoardDataByIdShouldThrowNotFoundWhenBoardDoesNotExist() throws Exception {
        //given
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

    @Test
    @DisplayName("getBoardDetailsById should return 200 when board details is collected")
    void getBoardDetailsByIdShouldReturnOk() throws Exception {
        //given
        final int boardId = 1;
        final String creatorEmail = "test@example.com";
        final List<BoardCardDto> successBoardCardsDtos = List.of(new BoardCardDto(1, "success", 0, creatorEmail, List.of("test success")));
        final List<BoardCardDto> failuresBoardCardsDtos = List.of(new BoardCardDto(2, "failure", 1, creatorEmail, List.of("test failure")));
        final List<BoardCardDto> kudosBoardCardsDtos = List.of(new BoardCardDto(3, "kudos", 2, creatorEmail, List.of("test kudos")));
        final List<BoardDetailsDto> boardDetailsDtos =
                List.of(BoardDetailsDto.createFrom(BoardCardsColumn.SUCCESS.getOrderNumber(), successBoardCardsDtos),
                        BoardDetailsDto.createFrom(BoardCardsColumn.FAILURES.getOrderNumber(), failuresBoardCardsDtos),
                        BoardDetailsDto.createFrom(BoardCardsColumn.KUDOS.getOrderNumber(), kudosBoardCardsDtos));

        final FirebaseToken firebaseToken = mock(FirebaseToken.class);

        //when
        when(firebaseToken.getEmail()).thenReturn(email);
        when(firebaseAuth.verifyIdToken(providedAccessToken)).thenReturn(firebaseToken);

        when(boardService.getBoardDetailsById(boardId, email)).thenReturn(boardDetailsDtos);

        //then
        this.mockMvc.perform(get(boardDataUrl + "/" + boardId + "/details").header(AUTHORIZATION, "Bearer " + providedAccessToken))
                .andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.*", hasSize(3)))
                .andExpect(result -> assertTrue(result.getResponse().getContentAsString()
                        .contains(boardDetailsDtos.get(0).getId().toString())))
                .andExpect(result -> assertTrue(result.getResponse().getContentAsString()
                        .contains(boardDetailsDtos.get(0).getBoardCards().get(0).getId().toString())))
                .andExpect(result -> assertTrue(result.getResponse().getContentAsString()
                        .contains(boardDetailsDtos.get(0).getBoardCards().get(0).getCardText())))
                .andExpect(result -> assertTrue(result.getResponse().getContentAsString()
                        .contains(boardDetailsDtos.get(0).getBoardCards().get(0).getBoardCardCreator())))
                .andExpect(result -> assertTrue(result.getResponse().getContentAsString()
                        .contains(boardDetailsDtos.get(0).getBoardCards().get(0).getActionTexts().get(0))))
                .andExpect(result -> assertTrue(result.getResponse().getContentAsString()
                        .contains(boardDetailsDtos.get(1).getId().toString())))
                .andExpect(result -> assertTrue(result.getResponse().getContentAsString()
                        .contains(boardDetailsDtos.get(1).getBoardCards().get(0).getId().toString())))
                .andExpect(result -> assertTrue(result.getResponse().getContentAsString()
                        .contains(boardDetailsDtos.get(1).getBoardCards().get(0).getCardText())))
                .andExpect(result -> assertTrue(result.getResponse().getContentAsString()
                        .contains(boardDetailsDtos.get(1).getBoardCards().get(0).getBoardCardCreator())))
                .andExpect(result -> assertTrue(result.getResponse().getContentAsString()
                        .contains(boardDetailsDtos.get(1).getBoardCards().get(0).getActionTexts().get(0))))
                .andExpect(result -> assertTrue(result.getResponse().getContentAsString()
                        .contains(boardDetailsDtos.get(2).getId().toString())))
                .andExpect(result -> assertTrue(result.getResponse().getContentAsString()
                        .contains(boardDetailsDtos.get(2).getBoardCards().get(0).getId().toString())))
                .andExpect(result -> assertTrue(result.getResponse().getContentAsString()
                        .contains(boardDetailsDtos.get(2).getBoardCards().get(0).getCardText())))
                .andExpect(result -> assertTrue(result.getResponse().getContentAsString()
                        .contains(boardDetailsDtos.get(2).getBoardCards().get(0).getBoardCardCreator())))
                .andExpect(result -> assertTrue(result.getResponse().getContentAsString()
                        .contains(boardDetailsDtos.get(2).getBoardCards().get(0).getActionTexts().get(0))));
    }

    @Test
    @DisplayName("getBoardDetailsById should return 400 when user has no access to the board")
    void getBoardDetailsByIdShouldThrowBadRequestWhenUserHasNoAccessToBoard() throws Exception {
        //given
        final int boardId = 1;
        final String exceptionMessage = "User has no access to board.";

        final FirebaseToken firebaseToken = mock(FirebaseToken.class);

        //when
        when(firebaseToken.getEmail()).thenReturn(email);
        when(firebaseAuth.verifyIdToken(providedAccessToken)).thenReturn(firebaseToken);

        when(boardService.getBoardDetailsById(boardId, email)).thenThrow(new BadRequestException(exceptionMessage));

        //then
        this.mockMvc.perform(get(boardDataUrl + "/" + boardId + "/details").header(AUTHORIZATION, "Bearer " + providedAccessToken))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof BadRequestException))
                .andExpect(result -> assertTrue(result.getResolvedException().getMessage().contains(exceptionMessage)));
    }

    @Test
    @DisplayName("getBoardDataById should return 404 when board does not exist")
    void getBoardDetailsByIdShouldThrowNotFoundWhenBoardDoesNotExist() throws Exception {
        //given
        final int boardId = 1;
        final String exceptionMessage = "Board not found.";

        final FirebaseToken firebaseToken = mock(FirebaseToken.class);
        // when
        when(firebaseToken.getEmail()).thenReturn(email);
        when(firebaseAuth.verifyIdToken(providedAccessToken)).thenReturn(firebaseToken);

        when(boardService.getBoardDetailsById(boardId, email)).thenThrow(new NotFoundException(exceptionMessage));

        //then
        this.mockMvc.perform(get(boardDataUrl + "/" + boardId + "/details")
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
        final FirebaseToken firebaseToken = mock(FirebaseToken.class);

        // when
        when(firebaseToken.getEmail()).thenReturn(email);
        when(firebaseAuth.verifyIdToken(providedAccessToken)).thenReturn(firebaseToken);

        // then
        final MvcResult result = mockMvc
                .perform(post(boardDataUrl)
                        .header(AUTHORIZATION, "Bearer " + providedAccessToken)
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
        final String boardName = null;

        final FirebaseToken firebaseToken = mock(FirebaseToken.class);

        // when
        when(firebaseToken.getEmail()).thenReturn(email);
        when(firebaseAuth.verifyIdToken(providedAccessToken)).thenReturn(firebaseToken);

        // then
        final MvcResult result = mockMvc
                .perform(post(boardDataUrl)
                        .header(AUTHORIZATION, "Bearer " + providedAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":" + boardName + "}")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest()).andReturn();

        final Exception resultException = result.getResolvedException();

        assertInstanceOf(MethodArgumentNotValidException.class, resultException);
    }

    @Test
    void patchBoardShouldReturnNotFoundWhenBoardDoesNotExist() throws Exception {
        // given
        final var boardDataUrl = this.boardDataUrl + "/1";
        final var boardName = "My first board.";
        final var maximumNumberOfVotes = 1;

        final FirebaseToken firebaseToken = mock(FirebaseToken.class);

        // when
        when(firebaseToken.getEmail()).thenReturn(email);
        when(firebaseAuth.verifyIdToken(providedAccessToken)).thenReturn(firebaseToken);
        when(boardService.patchBoard(eq(1), any(BoardPatchDto.class), anyString()))
                .thenThrow(new NotFoundException("Board not found!"));

        // then
        mockMvc.perform(patch(boardDataUrl)
                        .header(AUTHORIZATION, "Bearer " + providedAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + boardName + "\"," +
                                 "\"maximumNumberOfVotes\":\"" + maximumNumberOfVotes + "\" }")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void patchBoardShouldReturnBoardIfDataIsCorrect() throws Exception {
        // given
        final var boardDataUrl = this.boardDataUrl + "/1";
        final var boardName = "My first board.";
        final var maximumNumberOfVotes = 1;
        final BoardDto boardDto = BoardDto.builder()
                .id(1)
                .state(EnumStateDto.CREATED)
                .name(boardName)
                .numberOfVotes(maximumNumberOfVotes)
                .build();

        final FirebaseToken firebaseToken = mock(FirebaseToken.class);

        // when
        when(firebaseToken.getEmail()).thenReturn(email);
        when(firebaseAuth.verifyIdToken(providedAccessToken)).thenReturn(firebaseToken);
        when(boardService.patchBoard(eq(1), any(BoardPatchDto.class), anyString()))
                .thenReturn(boardDto);

        // then
        mockMvc.perform(patch(boardDataUrl)
                        .header(AUTHORIZATION, "Bearer " + providedAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"maximumNumberOfVotes\":\"" + maximumNumberOfVotes + "\" }")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("assignUsersToBoard should return 201 when users are assigned to board")
    void assignUsersToBoardShouldReturnCreated() throws Exception {
        final int boardId = 1;
        final String assignUsersUrl = boardDataUrl + "/" + boardId + "/users";
        final String failedEmail = "testfailemail@example.com";
        final List<String> usersEmails = List.of("testemail@example.com", failedEmail);
        final List<String> failedEmails = List.of(failedEmail);
        final FirebaseToken firebaseToken = mock(FirebaseToken.class);

        //when
        when(firebaseToken.getEmail()).thenReturn(email);
        when(firebaseAuth.verifyIdToken(providedAccessToken)).thenReturn(firebaseToken);
        when(boardService.assignUsersToBoard(boardId, usersEmails, email)).thenReturn(failedEmails);

        //then
        this.mockMvc.perform(post(assignUsersUrl)
                        .header(AUTHORIZATION, "Bearer " + providedAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new JSONArray(usersEmails).toString()))
                .andExpect(status().isCreated()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.*", hasSize(1))).andExpect(result -> assertTrue(
                        result.getResponse().getContentAsString().contains(failedEmails.get(0))));
    }

    @Test
    @DisplayName("assignUsersToBoard should return 400 when user is not an owner of the board")
    void assignUsersToBoardShouldThrowBadRequestWhenUserIsNotOwner() throws Exception {
        //given
        final int boardId = 1;
        final String assignUsersUrl = boardDataUrl + "/" + boardId + "/users";
        final List<String> usersEmails = List.of("testemail@example.com", "testfailemail@example.com");
        final String exceptionMessage = "User is not the board owner.";
        final FirebaseToken firebaseToken = mock(FirebaseToken.class);

        //when
        when(firebaseToken.getEmail()).thenReturn(email);
        when(firebaseAuth.verifyIdToken(providedAccessToken)).thenReturn(firebaseToken);
        when(boardService.assignUsersToBoard(boardId, usersEmails, email)).thenThrow(
                new BadRequestException(exceptionMessage));

        //then
        this.mockMvc.perform(post(assignUsersUrl)
                        .header(AUTHORIZATION, "Bearer " + providedAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new JSONArray(usersEmails).toString()))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof BadRequestException))
                .andExpect(result -> assertTrue(result.getResolvedException().getMessage().contains(exceptionMessage)));
    }

    @Test
    @DisplayName("assignUsersToBoard should return 404 when board does not exist")
    void assignUsersToBoardShouldThrowNotFoundWhenBoardDoesNotExist() throws Exception {
        //given
        final int boardId = 1;
        final String assignUsersUrl = boardDataUrl + "/" + boardId + "/users";
        final List<String> usersEmails = List.of("testemail@example.com", "testfailemail@example.com");
        final String exceptionMessage = "Board is not found.";
        final FirebaseToken firebaseToken = mock(FirebaseToken.class);

        // when
        when(firebaseToken.getEmail()).thenReturn(email);
        when(firebaseAuth.verifyIdToken(providedAccessToken)).thenReturn(firebaseToken);
        when(boardService.assignUsersToBoard(boardId, usersEmails, email)).thenThrow(
                new NotFoundException(exceptionMessage));

        //then
        this.mockMvc.perform(post(assignUsersUrl)
                        .header(AUTHORIZATION, "Bearer " + providedAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new JSONArray(usersEmails).toString()))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof NotFoundException))
                .andExpect(result -> assertTrue(result.getResolvedException().getMessage().contains(exceptionMessage)));
    }

    @Test
    @DisplayName("assignUsersToBoard should return 404 when user does not exist")
    void assignUsersToBoardShouldThrowNotFoundWhenUserDoesNotExist() throws Exception {
        //given
        final int boardId = 1;
        final String assignUsersUrl = boardDataUrl + "/" + boardId + "/users";
        final List<String> usersEmails = List.of("testemail@example.com", "testfailemail@example.com");
        final String exceptionMessage = "User is not found.";
        final FirebaseToken firebaseToken = mock(FirebaseToken.class);

        // when
        when(firebaseToken.getEmail()).thenReturn(email);
        when(firebaseAuth.verifyIdToken(providedAccessToken)).thenReturn(firebaseToken);
        when(boardService.assignUsersToBoard(boardId, usersEmails, email)).thenThrow(
                new NotFoundException(exceptionMessage));

        //then
        this.mockMvc.perform(post(assignUsersUrl)
                        .header(AUTHORIZATION, "Bearer " + providedAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new JSONArray(usersEmails).toString()))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof NotFoundException))
                .andExpect(result -> assertTrue(result.getResolvedException().getMessage().contains(exceptionMessage)));
    }

    @Test
    void getUserBoardsShouldReturnOkWhenUserExistsInDatabase() throws Exception {
        // given
        final List<BoardDto> dtoList = List.of(
                new BoardDto(1, EnumStateDto.CREATED, "test1", 1),
                new BoardDto(2, EnumStateDto.CREATED, "test2", 2)
        );
        final User user = new User();
        final FirebaseToken firebaseToken = mock(FirebaseToken.class);

        // when
        when(firebaseToken.getEmail()).thenReturn(email);
        when(userRepository.findUserByEmail(firebaseToken.getEmail())).thenReturn(Optional.of(user));
        when(firebaseAuth.verifyIdToken(providedAccessToken)).thenReturn(firebaseToken);
        when(boardService.getUserBoards(email)).thenReturn(dtoList);

        // then
        mockMvc.perform(get(boardDataUrl)
                        .header(AUTHORIZATION, "Bearer " + providedAccessToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.*", hasSize(2)))
                .andExpect(result -> verify(userRepository, never()).save(any()));
    }

    @Test
    void getUserBoardShouldSaveUserAndReturnOkWhenUserNotExistsInDatabase() throws Exception {
        // given
        final List<BoardDto> dtoList = List.of(
                new BoardDto(1, EnumStateDto.CREATED, "test1", 1),
                new BoardDto(2, EnumStateDto.CREATED, "test2", 2)
        );

        final FirebaseToken firebaseToken = mock(FirebaseToken.class);

        // when
        when(firebaseToken.getEmail()).thenReturn(email);
        when(userRepository.findUserByEmail(firebaseToken.getEmail())).thenReturn(Optional.empty());
        when(firebaseAuth.verifyIdToken(providedAccessToken)).thenReturn(firebaseToken);
        when(boardService.getUserBoards(email)).thenReturn(dtoList);

        // then
        mockMvc.perform(get(boardDataUrl)
                        .header(AUTHORIZATION, "Bearer " + providedAccessToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.*", hasSize(2)))
                .andExpect(result -> verify(userRepository, times(1)).findUserByEmail(email))
                .andExpect(result -> verify(userRepository, times(1)).save(any()));
    }

    @Test
    void createBoardShouldReturnCreatedWhenUserExistsInDatabaseAndBoardNameIsValid() throws Exception {
        // given
        final String boardName = "My first board.";

        final BoardDto boardDto = BoardDto.builder()
                .id(1004)
                .state(EnumStateDto.CREATED)
                .name(boardName)
                .build();

        final User user = new User();
        final FirebaseToken firebaseToken = mock(FirebaseToken.class);

        // when
        when(firebaseToken.getEmail()).thenReturn(email);
        when(userRepository.findUserByEmail(firebaseToken.getEmail())).thenReturn(Optional.of(user));
        when(firebaseAuth.verifyIdToken(providedAccessToken)).thenReturn(firebaseToken);
        when(boardService.createBoard(boardName, email)).thenReturn(boardDto);

        // then
        mockMvc.perform(post(boardDataUrl)
                        .header(AUTHORIZATION, "Bearer " + providedAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + boardName + "\"}")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(boardName));
    }

    @Test
    void createBoardShouldReturnCreatedWhenUserNotExistsInDatabaseAndBoardNameIsValid() throws Exception {
        // given
        final String boardName = "My first board.";

        final BoardDto boardDto = BoardDto.builder()
                .id(1004)
                .state(EnumStateDto.CREATED)
                .name(boardName)
                .build();

        final User user = new User();
        final FirebaseToken firebaseToken = mock(FirebaseToken.class);

        // when
        when(firebaseToken.getEmail()).thenReturn(email);
        when(userRepository.findUserByEmail(firebaseToken.getEmail())).thenReturn(Optional.of(user));
        when(firebaseAuth.verifyIdToken(providedAccessToken)).thenReturn(firebaseToken);
        when(boardService.createBoard(boardName, email)).thenReturn(boardDto);

        // then
        mockMvc.perform(post(boardDataUrl)
                        .header(AUTHORIZATION, "Bearer " + providedAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + boardName + "\"}")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(boardName));
    }

    @Test
    @DisplayName("assignUsersToBoard should return 201 when users are assigned to board")
    void assignUsersToBoardShouldReturnCreatedWhenUserExistsInDatabase() throws Exception {
        final int boardId = 1;
        final String assignUsersUrl = boardDataUrl + "/" + boardId + "/users";
        final String failedEmail = "testfailemail@example.com";
        final List<String> usersEmails = List.of("testemail@example.com", failedEmail);
        final List<String> failedEmails = List.of(failedEmail);
        final FirebaseToken firebaseToken = mock(FirebaseToken.class);
        final User user = new User();

        //when
        when(firebaseToken.getEmail()).thenReturn(email);
        when(userRepository.findUserByEmail(firebaseToken.getEmail())).thenReturn(Optional.of(user));
        when(firebaseAuth.verifyIdToken(providedAccessToken)).thenReturn(firebaseToken);
        when(boardService.assignUsersToBoard(boardId, usersEmails, email)).thenReturn(failedEmails);

        //then
        this.mockMvc.perform(post(assignUsersUrl)
                        .header(AUTHORIZATION, "Bearer " + providedAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new JSONArray(usersEmails).toString()))
                .andExpect(status().isCreated()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.*", hasSize(1))).andExpect(result -> assertTrue(
                        result.getResponse().getContentAsString().contains(failedEmails.get(0))));
    }

    @Test
    @DisplayName("assignUsersToBoard should return 201 when users are assigned to board")
    void assignUsersToBoardShouldReturnCreatedWhenUserNotExistsInDatabase() throws Exception {
        final int boardId = 1;
        final String assignUsersUrl = boardDataUrl + "/" + boardId + "/users";
        final String failedEmail = "testfailemail@example.com";
        final List<String> usersEmails = List.of("testemail@example.com", failedEmail);
        final List<String> failedEmails = List.of(failedEmail);
        final FirebaseToken firebaseToken = mock(FirebaseToken.class);
        final User user = new User();

        //when
        when(firebaseToken.getEmail()).thenReturn(email);
        when(userRepository.findUserByEmail(firebaseToken.getEmail())).thenReturn(Optional.of(user));
        when(firebaseAuth.verifyIdToken(providedAccessToken)).thenReturn(firebaseToken);
        when(boardService.assignUsersToBoard(boardId, usersEmails, email)).thenReturn(failedEmails);

        //then
        this.mockMvc.perform(post(assignUsersUrl)
                        .header(AUTHORIZATION, "Bearer " + providedAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new JSONArray(usersEmails).toString()))
                .andExpect(status().isCreated()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.*", hasSize(1))).andExpect(result -> assertTrue(
                        result.getResponse().getContentAsString().contains(failedEmails.get(0))));
    }
}
