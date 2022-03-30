package com.intive.patronage22.szczecin.retroboard.controller;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.intive.patronage22.szczecin.retroboard.configuration.security.SecurityConfig;
import com.intive.patronage22.szczecin.retroboard.dto.BoardCardActionRequestDto;
import com.intive.patronage22.szczecin.retroboard.dto.BoardCardDto;
import com.intive.patronage22.szczecin.retroboard.exception.BadRequestException;
import com.intive.patronage22.szczecin.retroboard.exception.NotFoundException;
import com.intive.patronage22.szczecin.retroboard.repository.UserRepository;
import com.intive.patronage22.szczecin.retroboard.service.BoardCardService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({BoardCardController.class, SecurityConfig.class})
class BoardCardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BoardCardService boardCardService;

    @MockBean
    private FirebaseAuth firebaseAuth;

    @MockBean
    private UserRepository userRepository;

    private static final String email = "test22@test.com";
    private static final String providedAccessToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9" +
            ".eyJzdWIiOiJzb21ldXNlciIsInJvbGVzIjpbIlJPTEVfVVNFUiJdLCJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjgwODAvbG9naW4ifQ." +
            "vDeQLA7Y8zTXaJW8bF08lkWzzwGi9Ll44HeMbOc22_o";
    private static final String url = "/api/v1/cards";

    @Test
    void addCardToTheBoardShouldReturnCreated() throws Exception {
        //given
        final Integer boardId = 1;
        final BoardCardDto requestDto = BoardCardDto.builder()
                .cardText("Some valid cardText test")
                .columnId(0)
                .build();

        final BoardCardDto responseDto = BoardCardDto.builder()
                .id(10)
                .cardText("Some valid cardText test")
                .columnId(0)
                .boardCardCreator(email)
                .actionTexts(List.of())
                .build();

        final FirebaseToken firebaseToken = mock(FirebaseToken.class);

        //when
        when(firebaseToken.getEmail()).thenReturn(email);
        when(firebaseAuth.verifyIdToken(providedAccessToken)).thenReturn(firebaseToken);
        when(boardCardService.createBoardCard(requestDto, boardId, email)).thenReturn(responseDto);

        //then
        mockMvc.perform(post(url + "/boards/" + boardId)
                        .header(AUTHORIZATION, "Bearer " + providedAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cardText\":\"" + requestDto.getCardText() + "\"," +
                                "\"columnId\":\"" + requestDto.getColumnId() + "\"}")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.*", hasSize(7)))
                .andExpect(result -> assertTrue(result.getResponse().getContentAsString()
                        .contains(responseDto.getId().toString())))
                .andExpect(result -> assertTrue(result.getResponse().getContentAsString()
                        .contains(responseDto.getCardText())))
                .andExpect(result -> assertTrue(result.getResponse().getContentAsString()
                        .contains(responseDto.getColumnId().toString())))
                .andExpect(result -> assertTrue(result.getResponse().getContentAsString()
                        .contains(responseDto.getBoardCardCreator())))
                .andExpect(result -> assertTrue(result.getResponse().getContentAsString().contains("[]")));
    }

    private static Stream<Arguments> provideInputsForPostedObjectValidation() {
        return Stream.of(
                Arguments.of("", "1", "boardCardDto.cardText"),
                Arguments.of(" ", "1", "boardCardDto.cardText"),
                Arguments.of("1234", "1", "boardCardDto.cardText"),
                Arguments.of("01234567890123456789012345678901234567890123456789012345678901234567890123456789" +
                        "0123456789012345678901234567890123456789123456789", "1", "boardCardDto.cardText"),
                Arguments.of("Some valid cardText test", "3", "boardCardDto.columnId"),
                Arguments.of("Some valid cardText test", "-1", "boardCardDto.columnId")
        );
    }

    @ParameterizedTest
    @MethodSource("provideInputsForPostedObjectValidation")
    void addCardToTheBoardShouldThrowBadRequestWhenPostedObjectIsNotValid(final String boardCardText,
                                                                          final String columnId,
                                                                          final String expectedIssue) throws Exception {
        //given
        final Integer boardId = 1;

        final FirebaseToken firebaseToken = mock(FirebaseToken.class);

        //when
        when(firebaseToken.getEmail()).thenReturn(email);
        when(firebaseAuth.verifyIdToken(providedAccessToken)).thenReturn(firebaseToken);

        //then
        final MvcResult result = mockMvc.perform(post(url + "/boards/" + boardId)
                        .header(AUTHORIZATION, "Bearer " + providedAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cardText\":\"" + boardCardText + "\"," +
                                "\"columnId\":\"" + Integer.valueOf(columnId) + "\"}")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest()).andReturn();

        final String errorMessage = result.getResolvedException().getMessage();
        assertTrue(errorMessage.contains(expectedIssue));
    }

    @Test
    void addCardToTheBoardShouldThrowBadRequestWhenBoardCardTextIsNull() throws Exception {
        //given
        final Integer boardId = 1;
        final String boardCardText = null;
        final String expectedExceptionMessage = "rejected value [null]";
        final Integer columnId = 1;

        final FirebaseToken firebaseToken = mock(FirebaseToken.class);

        //when
        when(firebaseToken.getEmail()).thenReturn(email);
        when(firebaseAuth.verifyIdToken(providedAccessToken)).thenReturn(firebaseToken);

        //then
        final MvcResult result = mockMvc.perform(post(url + "/boards/" + boardId)
                        .header(AUTHORIZATION, "Bearer " + providedAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cardText\":\"" + boardCardText + "\"," +
                                "\"columnId\":\"" + columnId + "\"}")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest()).andReturn();

        final String errorMessage = result.getResolvedException().getMessage();
        assertTrue(errorMessage.contains(expectedExceptionMessage));
    }

    @Test
    void addCardToTheBoardShouldThrowBadRequestWhenColumnIdIsNull() throws Exception {
        //given
        final Integer boardId = 1;
        final String boardCardText = "Some valid cardText test";
        final String expectedExceptionMessage = "rejected value [null]";
        final Integer columnId = null;

        final FirebaseToken firebaseToken = mock(FirebaseToken.class);

        //when
        when(firebaseToken.getEmail()).thenReturn(email);
        when(firebaseAuth.verifyIdToken(providedAccessToken)).thenReturn(firebaseToken);

        //then
        final MvcResult result = mockMvc.perform(post(url + "/boards/" + boardId)
                        .header(AUTHORIZATION, "Bearer " + providedAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cardText\":\"" + boardCardText + "\"," +
                                "\"columnId\":\"" + columnId + "\"}")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest()).andReturn();

        final String errorMessage = result.getResolvedException().getMessage();
        assertTrue(errorMessage.contains(expectedExceptionMessage));
    }

    @ParameterizedTest
    @ValueSource(strings = {"User is not found.", "User has no access to board", "Board state is not CREATED"})
    @DisplayName("addCardToTheBoard should throw Bad Request when user is not found, user is not the owner " +
            "or assigned to the board or board state is not CREATED")
    void addCardToTheBoardShouldThrowBadRequest(final String expectedExceptionMessage) throws Exception {
        //given
        final Integer boardId = 1;
        final BoardCardDto requestDto = BoardCardDto.builder()
                .cardText("Some valid cardText test")
                .columnId(0)
                .build();

        final FirebaseToken firebaseToken = mock(FirebaseToken.class);

        //when
        when(firebaseToken.getEmail()).thenReturn(email);
        when(firebaseAuth.verifyIdToken(providedAccessToken)).thenReturn(firebaseToken);
        when(boardCardService.createBoardCard(requestDto, boardId, email))
                .thenThrow(new BadRequestException(expectedExceptionMessage));

        //then
        mockMvc.perform(post(url + "/boards/" + boardId)
                        .header(AUTHORIZATION, "Bearer " + providedAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cardText\":\"" + requestDto.getCardText() + "\"," +
                                "\"columnId\":\"" + requestDto.getColumnId() + "\"}")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(result ->
                        assertTrue(result.getResolvedException().getMessage().contains(expectedExceptionMessage)));
    }

    @Test
    void addCardToTheBoardShouldThrowNotFoundWhenBoardIsNotFound() throws Exception {
        //given
        final Integer boardId = 1;
        final BoardCardDto requestDto = BoardCardDto.builder()
                .cardText("Some valid cardText test")
                .columnId(0)
                .build();
        final String expectedExceptionMessage = "Board not found";

        final FirebaseToken firebaseToken = mock(FirebaseToken.class);

        //when
        when(firebaseToken.getEmail()).thenReturn(email);
        when(firebaseAuth.verifyIdToken(providedAccessToken)).thenReturn(firebaseToken);
        when(boardCardService.createBoardCard(requestDto, boardId, email))
                .thenThrow(new NotFoundException(expectedExceptionMessage));

        //then
        mockMvc.perform(post(url + "/boards/" + boardId)
                        .header(AUTHORIZATION, "Bearer " + providedAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cardText\":\"" + requestDto.getCardText() + "\"," +
                                "\"columnId\":\"" + requestDto.getColumnId() + "\"}")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(result ->
                        assertTrue(result.getResolvedException().getMessage().contains(expectedExceptionMessage)));
    }

    @Test
    void addVoteShouldReturnCreated() throws Exception {
        //given
        final int cardId = 1;
        final String voteUrl = url + "/" + cardId + "/votes";
        final String responseMapString = "remainingVotes";
        final Integer responseMapInteger = 0;
        final Map<String, Integer> response = Map.of(responseMapString, responseMapInteger);

        final FirebaseToken firebaseToken = mock(FirebaseToken.class);

        //when
        when(firebaseToken.getEmail()).thenReturn(email);
        when(firebaseAuth.verifyIdToken(providedAccessToken)).thenReturn(firebaseToken);

        when(boardCardService.addVote(cardId, email)).thenReturn(response);

        //then
        mockMvc.perform(post(voteUrl).header(AUTHORIZATION, "Bearer " + providedAccessToken))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.*", hasSize(1)))
                .andExpect(result -> assertTrue(result.getResponse().getContentAsString()
                        .contains(responseMapString)))
                .andExpect(result -> assertTrue(result.getResponse().getContentAsString()
                        .contains(responseMapInteger.toString())));
    }

    @ParameterizedTest
    @ValueSource(strings = {"No more votes", "Wrong state of board", "User not found", "Board not exist",
            "User not assigned to board"})
    @DisplayName(
            "addVote should throw Bad Request when there's no more votes left, board state is not VOTING, user/board is " +
            "not found or user is not assigned to board nor the owner.")
    void addVoteShouldThrowBadRequest(final String exceptionMessage) throws Exception {
        //given
        final int cardId = 1;
        final String voteUrl = url + "/" + cardId + "/votes";

        final FirebaseToken firebaseToken = mock(FirebaseToken.class);

        //when
        when(firebaseToken.getEmail()).thenReturn(email);
        when(firebaseAuth.verifyIdToken(providedAccessToken)).thenReturn(firebaseToken);

        when(boardCardService.addVote(cardId, email)).thenThrow(new BadRequestException(exceptionMessage));

        //then
        mockMvc.perform(
                        post(voteUrl).header(AUTHORIZATION, "Bearer " + providedAccessToken).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException().getMessage().contains(exceptionMessage)));
    }

    @Test
    void addVoteShouldThrowNotFoundWhenBoardCardDoesNotExist() throws Exception {
        //given
        final int cardId = 1;
        final String voteUrl = url + "/" + cardId + "/votes";
        final String exceptionMessage = "Card not found";

        final FirebaseToken firebaseToken = mock(FirebaseToken.class);
        //when
        when(firebaseToken.getEmail()).thenReturn(email);
        when(firebaseAuth.verifyIdToken(providedAccessToken)).thenReturn(firebaseToken);

        when(boardCardService.addVote(cardId, email)).thenThrow(new NotFoundException(exceptionMessage));

        //then
        mockMvc.perform(
                        post(voteUrl).header(AUTHORIZATION, "Bearer " + providedAccessToken).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException().getMessage().contains(exceptionMessage)));
    }

    @Test
    void removeVoteShouldReturnOk() throws Exception {
        //given
        final int cardId = 1;
        final String voteUrl = url + "/" + cardId + "/votes";
        final String responseMapString = "remainingVotes";
        final Integer responseMapInteger = 0;
        final Map<String, Integer> response = Map.of(responseMapString, responseMapInteger);

        final FirebaseToken firebaseToken = mock(FirebaseToken.class);

        //when
        when(firebaseToken.getEmail()).thenReturn(email);
        when(firebaseAuth.verifyIdToken(providedAccessToken)).thenReturn(firebaseToken);
        when(boardCardService.removeVote(cardId, email)).thenReturn(response);

        //then
        mockMvc.perform(delete(voteUrl).header(AUTHORIZATION, "Bearer " + providedAccessToken))
                .andExpect(status().isOk());
    }

    @Test
    void removeVoteShouldThrowNotFoundWhenBoardCardDoesNotExist() throws Exception {
        //given
        final int cardId = 1;
        final String voteUrl = url + "/" + cardId + "/votes";
        final String exceptionMessage = "Card not found";

        final FirebaseToken firebaseToken = mock(FirebaseToken.class);

        //when
        when(firebaseToken.getEmail()).thenReturn(email);
        when(firebaseAuth.verifyIdToken(providedAccessToken)).thenReturn(firebaseToken);
        when(boardCardService.removeVote(cardId, email)).thenThrow(new NotFoundException(exceptionMessage));

        //then
        mockMvc.perform(delete(voteUrl).header(AUTHORIZATION, "Bearer " + providedAccessToken))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException().getMessage().contains(exceptionMessage)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Wrong state of board", "User not found", "Board not exist",
            "User not assigned to board", "User has no votes to remove"})
    @DisplayName(
            "removeVote should throw Bad Request when board state is not VOTING, user/board is " +
                    "not found, user is not assigned to board nor the owner or there's no more votes to remove")
    void removeVoteShouldThrowException(final String exceptionMessage) throws Exception {
        //given
        final int cardId = 1;
        final String voteUrl = url + "/" + cardId + "/votes";

        final FirebaseToken firebaseToken = mock(FirebaseToken.class);

        //when
        when(firebaseToken.getEmail()).thenReturn(email);
        when(firebaseAuth.verifyIdToken(providedAccessToken)).thenReturn(firebaseToken);
        when(boardCardService.removeVote(cardId, email)).thenThrow(new BadRequestException(exceptionMessage));

        //then
        mockMvc.perform(delete(voteUrl).header(AUTHORIZATION, "Bearer " + providedAccessToken))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException().getMessage().contains(exceptionMessage)));
    }

    @Test
    @DisplayName("Add card action should return created.")
    void addCardActionShouldReturnCreated() throws Exception {
        //given
        final int cardId = 1;
        final BoardCardActionRequestDto cardActionText = new BoardCardActionRequestDto("test text");
        final String addCardActionUrl = url + "/" + cardId + "/actions";
        final FirebaseToken firebaseToken = mock(FirebaseToken.class);

        //when
        when(firebaseToken.getEmail()).thenReturn(email);
        when(firebaseAuth.verifyIdToken(providedAccessToken)).thenReturn(firebaseToken);
        when(boardCardService.addCardAction(cardId, email, cardActionText)).thenReturn(null);

        //then
        mockMvc.perform(post(addCardActionUrl)
                        .header(AUTHORIZATION, "Bearer " + providedAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"text\":\"" + "test text" + "\" }")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
    }

    @Test
    void removeActionShouldReturnOk() throws Exception {
        // given
        final Integer actionId = 1;
        final String actionsUrl = url + "/actions/" + actionId;
        final FirebaseToken firebaseToken = mock(FirebaseToken.class);

        // when
        when(firebaseToken.getEmail()).thenReturn(email);
        when(firebaseAuth.verifyIdToken(providedAccessToken)).thenReturn(firebaseToken);
        doNothing().when(boardCardService).removeAction(actionId, email);

        // then
        mockMvc.perform(delete(actionsUrl).header(AUTHORIZATION, "Bearer " + providedAccessToken))
                .andExpect(status().isOk());
    }
}