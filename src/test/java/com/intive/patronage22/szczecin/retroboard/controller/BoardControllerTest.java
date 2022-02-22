package com.intive.patronage22.szczecin.retroboard.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.Authentication;
import com.google.firebase.auth.AbstractFirebaseAuth;
import com.google.firebase.auth.FirebaseAuth;
import com.intive.patronage22.szczecin.retroboard.configuration.security.SecurityConfig;
import com.intive.patronage22.szczecin.retroboard.dto.BoardCardDto;
import com.intive.patronage22.szczecin.retroboard.dto.BoardCardsColumn;
import com.intive.patronage22.szczecin.retroboard.dto.BoardDataDto;
import com.intive.patronage22.szczecin.retroboard.dto.BoardDto;
import com.intive.patronage22.szczecin.retroboard.dto.EnumStateDto;
import com.intive.patronage22.szczecin.retroboard.exception.BoardNotFoundException;
import com.intive.patronage22.szczecin.retroboard.exception.MissingPermissionsException;
import com.intive.patronage22.szczecin.retroboard.exception.UserNotFoundException;
import com.intive.patronage22.szczecin.retroboard.service.BoardService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Locale;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest({BoardController.class, SecurityConfig.class})
class BoardControllerTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @MockBean private BoardService boardService;

    @MockBean private AuthenticationManager authenticationManager;

    @Autowired private RestTemplate restTemplate;

    private MockRestServiceServer firebaseRestServiceServer;

    @PostConstruct
    public void postConstruct() {
        firebaseRestServiceServer = MockRestServiceServer.createServer(restTemplate);
    }

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
    void getBoardDataByIdShouldReturnOk() throws Exception {
        //given
        final String loginUrl = "/login";
        final String username = "testuser@example.com";
        final String password = "1234";
        final String encodedPassword = "$2a$10$A0IKJqSv.cSqXb7BuIPw4.GvP1U3VPUIRvkAigPVr6HipH.R3nGLO";
        final String boardDataUrl = "/boards";
        final int boardId = 1;

        final BoardDto boardDto = new BoardDto(1, EnumStateDto.CREATED, "test1");
        final List<String> actionTexts = List.of("action text 1", "action text 2");
        final BoardCardDto boardCardDataDto =
                new BoardCardDto(2, "cardText", BoardCardsColumn.SUCCESS, username, actionTexts);
        final BoardCardDto boardCardDataDto1 =
                new BoardCardDto(3, "cardText3", BoardCardsColumn.FAILURES, username, actionTexts);
        final BoardDataDto boardDataDto = new BoardDataDto(boardDto, List.of(boardCardDataDto, boardCardDataDto1));

        //when
        when(boardService.getBoardDataById(boardId, username)).thenReturn(boardDataDto);
//        when(inMemoryUserDetailsManager.loadUserByUsername(username)).thenReturn(
//                createExistingUser(username, encodedPassword, "user"));

        //then
        final String token = getToken(loginUrl, username, password);
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
    void getBoardDataByIdShouldThrowBadRequestWhenUserDoesntHavePermissions() throws Exception {
        //given
        final String loginUrl = "/login";
        final String username = "testuser@example.com";
        final String password = "1234";
        final String boardDataUrl = "/boards";
        final int boardId = 1;
        final String expectedExceptionMessage = "User has no permission to view data.";

        //when
        when(boardService.getBoardDataById(boardId, username)).thenThrow(MissingPermissionsException.class);
//        when(inMemoryUserDetailsManager.loadUserByUsername(username)).thenReturn(
//                createExistingUser(username, encodedPassword, "user"));

        //then
        final String token = getToken(loginUrl, username, password);
        this.mockMvc.perform(get(boardDataUrl + "/" + boardId).header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ResponseStatusException))
                .andExpect(result -> assertTrue(
                        result.getResolvedException().getMessage().contains(expectedExceptionMessage)));
    }

    @Test
    @DisplayName("getBoardDataById should return 404 when board does not exist")
    void getBoardDataByIdShouldThrowNotFoundWhenBoardDoesNotExist() throws Exception {
        //given
        final String loginUrl = "/login";
        final String email = "someuser@gmail.com";
        final String password = "1234";
        final String boardDataUrl = "/boards";
//        final String tokenString = "eyJhbGciOiJSUzI1NiIsImtpZCI6IjI3ZGRlMTAyMDAyMGI3OGZiODc2ZDdiMjVlZDhmMGE5Y2UwNmRiNGQiLCJ0eXAiOiJKV1QifQ.eyJpc3MiOiJodHRwczovL3NlY3VyZXRva2VuLmdvb2dsZS5jb20vcGF0cm9uYWdlMjAyMi1yZXRybyIsImF1ZCI6InBhdHJvbmFnZTIwMjItcmV0cm8iLCJhdXRoX3RpbWUiOjE2NDU0ODY2MjYsInVzZXJfaWQiOiI0U01Rc2lHQm90UFl5RXNoSDVuUXlCY1lwVzgyIiwic3ViIjoiNFNNUXNpR0JvdFBZeUVzaEg1blF5QmNZcFc4MiIsImlhdCI6MTY0NTQ4NjYyNiwiZXhwIjoxNjQ1NDkwMjI2LCJlbWFpbCI6InBhdHJ5ay56ZXQrdGVzdDFAZ21haWwuY29tIiwiZW1haWxfdmVyaWZpZWQiOmZhbHNlLCJmaXJlYmFzZSI6eyJpZGVudGl0aWVzIjp7ImVtYWlsIjpbInBhdHJ5ay56ZXQrdGVzdDFAZ21haWwuY29tIl19LCJzaWduX2luX3Byb3ZpZGVyIjoicGFzc3dvcmQifX0.PQwm5BNvn1BYKDZ7jJGuKwtnFQOo98YhEzVpnrotjCQaWBrqomnhjcvxUeUIm4smYi1pX3fpHw74WeALyQ8tZBrmoqTW6VyUpqaYGzeHyQe5D7Ot6Z7t7svto6eeGZCYkgxTeXZwK1Acpqvlsxnu4NCoin115g6A4BTyzJwiqlVxI2_c4dZxcugvY1m1oT3V1Dn-p7eqcTPLFcvMrGP6aBr9Q-ZNEWcopPrKgRkTKjL7lKmOmP8UH4jMNFYUs2k0LOecelRVEMlFzdM-aTJ4yWhZ135dkjNzRms-NMd1QqGTowgpfj-WiV6bXvmjguvDXcqYE_kYmU0rZBgbdkvZFg";
        final int boardId = 1;
        final String expectedExceptionMessage = "Board is not found.";

        final UsernamePasswordAuthenticationToken tokenToAuth = new UsernamePasswordAuthenticationToken(email, password);
        firebaseRestServiceServer.expect(method(HttpMethod.POST))
                .andRespond(withSuccess("{\n" +
                                        "  \"kind\": \"identitytoolkit#VerifyPasswordResponse\",\n" +
                                        "  \"localId\": \"4SMQsiGBotPYyEshH5nQyBcYpW82\",\n" +
                                        "  \"email\": \"" + email + "\",\n" +
                                        "  \"displayName\": \"\",\n" +
                                        "  \"idToken\": \"" + tokenToAuth +"\",\n" +
                                        "  \"registered\": true,\n" +
                                        "  \"refreshToken\": \"[REFRESH_TOKEN]\",\n" +
                                        "  \"expiresIn\": \"3600\"\n" + "}", MediaType.APPLICATION_JSON));



        // when
        when(authenticationManager.authenticate(tokenToAuth)).thenReturn(tokenToAuth);
        when(boardService.getBoardDataById(boardId, email)).thenThrow(BoardNotFoundException.class);

        //then
        final String token = getToken(loginUrl, email, password);
        this.mockMvc.perform(get(boardDataUrl + "/" + boardId).header("Authorization", token))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ResponseStatusException))
                .andExpect(result -> assertTrue(
                        result.getResolvedException().getMessage().contains(expectedExceptionMessage)));
    }

    private UserDetails createExistingUser(final String username, final String encodedPassword, final String role) {
        return User.withUsername(username).password(encodedPassword).roles(role.toUpperCase(Locale.ROOT)).build();
    }


    private String getToken(final String loginUrl, final String email, final String password) throws Exception {
        return mockMvc.perform(post(loginUrl).param("email", email).param("password", password).param("returnSecureToken", "true")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)).andReturn().getResponse()
                .getHeader("Authorization");
    }
}
