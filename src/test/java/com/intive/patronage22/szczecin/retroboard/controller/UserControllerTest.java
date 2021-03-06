package com.intive.patronage22.szczecin.retroboard.controller;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.intive.patronage22.szczecin.retroboard.configuration.security.SecurityConfig;
import com.intive.patronage22.szczecin.retroboard.exception.MissingFieldException;
import com.intive.patronage22.szczecin.retroboard.exception.UserAlreadyExistException;
import com.intive.patronage22.szczecin.retroboard.repository.UserRepository;
import com.intive.patronage22.szczecin.retroboard.service.UserService;
import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import java.util.List;
import java.util.stream.Stream;

import static com.intive.patronage22.szczecin.retroboard.configuration.security.WebSecurityConfig.URL_LOGIN;
import static com.intive.patronage22.szczecin.retroboard.configuration.security.WebSecurityConfig.URL_REGISTER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({UserController.class, SecurityConfig.class})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private FirebaseAuth firebaseAuth;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private RestTemplate restTemplate;

    private MockRestServiceServer firebaseRestServiceServer;

    private static final String providedAccessToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJzb21ldXNlciIsIn" +
                                                      "JvbGVzIjpbIlJPTEVfVVNFUiJdLCJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjgwO" +
                                                      "DAvbG9naW4ifQ.vDeQLA7Y8zTXaJW8bF08lkWzzwGi9Ll44HeMbOc22_o";

    @PostConstruct
    public void postContruct() {
        firebaseRestServiceServer = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    void registerShouldReturnCreatedWhenUserInputsAreValid() throws Exception {
        // given
        final String email = "someuser@test.com";
        final String displayName = "someuser";
        final String password = "123456";

        final UserDetails createdUser = User
                .withUsername(email)
                .password(password)
                .roles("USER")
                .build();

        final User returnedUser = (User) createdUser;
        returnedUser.eraseCredentials();

        // when
        when(userService.register(email, password, displayName)).thenReturn(returnedUser);

        // then
        mockMvc
                .perform(post(URL_REGISTER)
                        .param("email", email)
                        .param("password", password)
                        .param("displayName", displayName)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(jsonPath("$.username").value(email))
                .andExpect(jsonPath("$.password").value(IsNull.nullValue()))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
    }

    @Test
    void registerShouldReturnConflictWhenUserAlreadyExists() throws Exception {
        // given
        final String email = "someuser@test.com";
        final String displayName = "someuser";
        final String password = "123456";

        // when
        when(userService.register(email, password, displayName))
                .thenThrow(new UserAlreadyExistException("User already exist"));

        // then
        final MvcResult result = mockMvc
                .perform(post(URL_REGISTER)
                        .param("email", email)
                        .param("password", password)
                        .param("displayName", displayName)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isConflict())
                .andReturn();

        final Exception resultException = result.getResolvedException();

        assertInstanceOf(UserAlreadyExistException.class, resultException);
        assertEquals("User already exist", resultException.getMessage());
    }

    private static Stream<Arguments> provideStringsForUserInputsValidation() {
        return Stream.of(
                Arguments.of("someuser@test.com", "someuser", "12345", "register.password"),
                Arguments.of("someuser@test.com", "someuser",
                        "01234567890123456789012345678901234567890123456789012345678912345", "register.password"),
                Arguments.of("someuser@test.com", "someuser", "", "register.password"),
                Arguments.of("someuser@test.com", "someuser", " ", "register.password"),
                Arguments.of("someuser@test.com", "someuser", "null", "register.password"),
                Arguments.of("", "someuser", "123456", "register.email"),
                Arguments.of(" ", "someuser", "123456", "register.email"),
                Arguments.of("01234567890123456789012345678901234567890123456789012345678912345@test.com", "someuser",
                        "123456", "register.email"),
                Arguments.of("someuser@.com", "someuser", "123456", "register.email"),
                Arguments.of("someuser@test.", "someuser", "123456", "register.email"),
                Arguments.of("someuser@", "someuser", "123456", "register.email"),
                Arguments.of("someuser@test.com", "", "123456", "register.displayName"),
                Arguments.of("someuser@test.com", " ", "123456", "register.displayName"),
                Arguments.of("someuser@test.com", "123", "123456", "register.displayName"),
                Arguments.of("someuser@test.com", ".", "123456", "register.displayName"),
                Arguments.of("someuser@test.com", "01234567890123456789012345678901234567890123456789012345678912345",
                        "123456", "register.displayName")
        );
    }

    @ParameterizedTest
    @MethodSource("provideStringsForUserInputsValidation")
    void registerShouldThrowBadRequestWhenUserInputsAreNotValid(final String email, final String displayName,
                                                                final String password, final String expectedIssue)
            throws Exception {

        // then
        final MvcResult result = mockMvc
                .perform(post(URL_REGISTER)
                        .param("email", email)
                        .param("password", password)
                        .param("displayName", displayName)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isBadRequest()).andReturn();

        final Exception resultException = result.getResolvedException();

        assertInstanceOf(ConstraintViolationException.class, resultException);

        ((ConstraintViolationException) resultException)
                .getConstraintViolations()
                .stream()
                .map(ConstraintViolation::getPropertyPath)
                .map(Path::toString)
                .forEach(c -> assertEquals(expectedIssue, c));
    }

    @Test
    void loginShouldReturnAccessTokenWhenUserCredentialsAreCorrect() throws Exception {
        // given
        final String email = "someuser@test.com";
        final String password = "1234";
        final UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(email, password);
        firebaseRestServiceServer.expect(method(HttpMethod.POST))
                .andRespond(withSuccess("{\n" +
                        "  \"kind\": \"identitytoolkit#VerifyPasswordResponse\",\n" +
                        "  \"localId\": \"4SMQsiGBotPYyEshH5nQyBcYpW82\",\n" +
                        "  \"email\": \"" + email + "\",\n" +
                        "  \"displayName\": \"\",\n" +
                        "  \"idToken\": \"[ID_TOKEN]\",\n" +
                        "  \"registered\": true,\n" +
                        "  \"refreshToken\": \"[REFRESH_TOKEN]\",\n" +
                        "  \"expiresIn\": \"3600\"\n" + "}", MediaType.APPLICATION_JSON));

        // when
        when(authenticationManager.authenticate(token)).thenReturn(token);

        //then
        mockMvc
                .perform(post(URL_LOGIN)
                        .param("email", email)
                        .param("password", password)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(header().string(AUTHORIZATION, "Bearer [ID_TOKEN]"));
    }

    @Test
    void loginShouldReturnUnauthorizedWhenUserNotFound() throws Exception {
        // given
        final String email = "someuser@test.com";
        final String password = "1234";
        final UsernameNotFoundException expectedException = new UsernameNotFoundException(email);
        final UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(email, password);
        firebaseRestServiceServer.expect(method(HttpMethod.POST))
                .andRespond(withBadRequest()
                        .body("{\n" +
                                "  \"error\": {\n" +
                                "    \"code\": 400,\n" +
                                "    \"message\": \"EMAIL_NOT_FOUND\",\n" +
                                "    \"errors\": [\n" +
                                "      {\n" +
                                "        \"message\": \"EMAIL_NOT_FOUND\",\n" +
                                "        \"domain\": \"global\",\n" +
                                "        \"reason\": \"invalid\"\n" +
                                "      }\n" +
                                "    ]\n" +
                                "  }\n" +
                                "}").contentType(MediaType.APPLICATION_JSON));

        // when
        when(authenticationManager.authenticate(token)).thenThrow(expectedException);

        // then
        mockMvc
                .perform(post(URL_LOGIN)
                        .param("email", email)
                        .param("password", password)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error_message").value("Email not found."));
    }

    @ParameterizedTest
    @ValueSource(strings = {"test", "t@e.p", "123@", "test@o2.", "test@o2.l", "", " "})
    void loginShouldReturnBadRequestWhenEmailIsInvalid(final String email) throws Exception {
        // given
        final String password = "1234";

        // when then
        mockMvc
                .perform(post(URL_LOGIN)
                        .param("email", email)
                        .param("password", password)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error_message").value("Invalid email."));
    }

    @Test
    void loginShouldReturnBadRequestWhenEmailIsNull() throws Exception {
        // given
        final String email = null;
        final String password = "1234";

        // when then
        mockMvc
                .perform(post(URL_LOGIN)
                        .param("email", email)
                        .param("password", password)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error_message").value("Invalid email."));
    }

    @Test
    void loginShouldReturnUnauthorizedWhenPasswordIsInvalid() throws Exception {
        // given
        final String email = "someuser@test.com";
        final String password = "1234";
        final BadCredentialsException expectedException = new BadCredentialsException("Invalid password.");
        final UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(email, password);
        firebaseRestServiceServer.expect(method(HttpMethod.POST))
                .andRespond(withBadRequest()
                        .body("{\n" +
                                "  \"error\": {\n" + "    \"code\": 400,\n" +
                                "    \"message\": \"INVALID_PASSWORD\",\n" +
                                "    \"errors\": [\n" +
                                "      {\n" +
                                "        \"message\": \"INVALID_PASSWORD\",\n" +
                                "        \"domain\": \"global\",\n" +
                                "        \"reason\": \"invalid\"\n" +
                                "      }\n" +
                                "    ]\n" +
                                "  }\n" +
                                "}").contentType(MediaType.APPLICATION_JSON));

        // when
        when(authenticationManager.authenticate(token)).thenThrow(expectedException);

        // then
        mockMvc
                .perform(post(URL_LOGIN)
                        .param("email", email)
                        .param("password", password)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error_message").value("Invalid password."));
    }

    @Test
    void loginShouldReturnBadRequestWhenPasswordIsMissing() throws Exception {
        // given
        final String email = "someuser@test.com";
        final String password = null;
        final MissingFieldException expectedException = new MissingFieldException("Missing password.");
        final UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(email, password);
        firebaseRestServiceServer.expect(method(HttpMethod.POST))
                .andRespond(withBadRequest()
                        .body("{\n" +
                                "  \"error\": {\n" +
                                "    \"code\": 400,\n" +
                                "    \"message\": \"MISSING_PASSWORD\",\n" +
                                "    \"errors\": [\n" +
                                "      {\n" +
                                "        \"message\": \"MISSING_PASSWORD\",\n" +
                                "        \"domain\": \"global\",\n" +
                                "        \"reason\": \"invalid\"\n" +
                                "      }\n" +
                                "    ]\n" +
                                "  }\n" +
                                "}").contentType(MediaType.APPLICATION_JSON));

        // when
        when(authenticationManager.authenticate(token)).thenThrow(expectedException);

        // then
        mockMvc
                .perform(post(URL_LOGIN)
                        .param("email", email)
                        .param("password", password)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error_message").value("Missing password."));
    }

    @Test
    void searchShouldReturnOkWhenEmailsIsShorterThan64Characters() throws Exception {
         // given
        final String url = "/api/v1/users";
        final String providedEmail = "test";
        final List<String> emails = List.of("test12@plo.com", "sodttest2@tyk.pl", "sodniktest@sok.com");

        final FirebaseToken firebaseToken = mock(FirebaseToken.class);

        // when
        when(firebaseAuth.verifyIdToken(providedAccessToken)).thenReturn(firebaseToken);
        when(userService.search(providedEmail)).thenReturn(emails);

        // then
        mockMvc
                .perform(get(url)
                        .header(AUTHORIZATION, "Bearer " + providedAccessToken)
                        .param("email", providedEmail))
                .andExpect(status().isOk());
    }

    @Test
    void searchShouldReturnBadRequestWhenEmailIsMoreThan64Characters() throws Exception {
        // given
        final String url = "/api/v1/users";
        final String providedEmail = "testd3123cczadas4131hbdjkasnduhascnklnasdnaklsndjkcbjans" +
                                     "cssijxcva723nc312ddasnvcxmwrw@test.com";

        final FirebaseToken firebaseToken = mock(FirebaseToken.class);

        // when
        when(firebaseAuth.verifyIdToken(providedAccessToken)).thenReturn(firebaseToken);

        // then
        mockMvc
                .perform(get(url)
                        .header(AUTHORIZATION, "Bearer " + providedAccessToken)
                        .param("email", providedEmail))
                .andExpect(status().isBadRequest());
    }

    @Test
    void searchShouldReturnBadRequestWhenEmailIsShorterThan3Characters() throws Exception {
        // given
        final String url = "/api/v1/users";
        final String providedEmail = "pl";

        final FirebaseToken firebaseToken = mock(FirebaseToken.class);

        // when
        when(firebaseAuth.verifyIdToken(providedAccessToken)).thenReturn(firebaseToken);

        // then
        mockMvc
                .perform(get(url)
                        .header(AUTHORIZATION, "Bearer " + providedAccessToken)
                        .param("email", providedEmail))
                .andExpect(status().isBadRequest());
    }

    @Test
    void searchShouldReturnBadRequestWhenEmailIsBlank() throws Exception {
        // given
        final String url = "/api/v1/users";
        final String providedEmail = "         ";

        final FirebaseToken firebaseToken = mock(FirebaseToken.class);

        // when
        when(firebaseAuth.verifyIdToken(providedAccessToken)).thenReturn(firebaseToken);

        // then
        mockMvc
                .perform(get(url)
                        .header(AUTHORIZATION, "Bearer " + providedAccessToken)
                        .param("email", providedEmail))
                .andExpect(status().isBadRequest());
    }
}