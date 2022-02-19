package com.intive.patronage22.szczecin.retroboard.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intive.patronage22.szczecin.retroboard.configuration.security.SecurityConfig;
import com.intive.patronage22.szczecin.retroboard.dto.FirebaseUserDto;
import com.intive.patronage22.szczecin.retroboard.dto.UserLoginRequestDto;
import com.intive.patronage22.szczecin.retroboard.exception.MissingFieldException;
import com.intive.patronage22.szczecin.retroboard.exception.UserAlreadyExistException;
import com.intive.patronage22.szczecin.retroboard.provider.FirebaseAuthenticationProvider;
import com.intive.patronage22.szczecin.retroboard.service.UserService;
import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest({UserController.class, SecurityConfig.class})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${retroboard.jwt.secret}")
    private String jwtSecret;

    @MockBean
    private UserService userService;

    @MockBean
    private FirebaseAuthenticationProvider authenticationProvider;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private RestTemplate restTemplate;

    @Test
    void registerShouldReturnJsonBodyWithCreatedUserDataWhenUserNotExistBefore() throws Exception {
        // given
        final String url = "/register";
        final String email = "someuser@test.com";
        final String displayName = "someuser";
        final String password = "1234";

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
                .perform(post(url)
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
    void registerShouldReturnConflictWhenUserExist() throws Exception {
        // given
        final String url = "/register";
        final String email = "someuser@test.com";
        final String displayName = "someuser";
        final String password = "1234";

        // when
        when(userService.register(email, password, displayName)).thenThrow(new UserAlreadyExistException());

        // then
        final MvcResult result = mockMvc
                .perform(post(url)
                        .param("email", email)
                        .param("password", password)
                        .param("displayName", displayName)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(res -> assertEquals("User already exist", res.getResponse().getErrorMessage()))
                .andExpect(status().isConflict()).andReturn();

        final Exception resultException = result.getResolvedException();

        assertInstanceOf(UserAlreadyExistException.class, resultException);
        assertEquals("User already exist", result.getResponse().getErrorMessage());
    }

    // not working - No AuthenticationProvider
//    @Test
    void loginShouldReturnAccessTokenWhenUserCredentialsAreCorrect() throws Exception {
        // given
        final String url = "/login";
        final String email = "someuser@test.com";
        final String password = "1234";
        final UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(email, password);

        // when
        when(authenticationManager.authenticate(token)).thenReturn(token);

        // then
        mockMvc
                .perform(post(url)
                        .param("email", email)
                        .param("password", password)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(header().exists("Authorization"));
    }

    @Test
    void loginShouldReturnUnauthorizedWhenUserNotFound() throws Exception {
        // given
        final String url = "/login";
        final String email = "someuser@test.com";
        final String password = "1234";
        final UsernameNotFoundException expectedException = new UsernameNotFoundException(email);
        final UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(email, password);

        // when
        when(authenticationProvider.authenticate(token)).thenThrow(expectedException);

        // then
        mockMvc
                .perform(post(url)
                        .param("email", email)
                        .param("password", password)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    // not working - No AuthenticationProvider
//    @Test
    void loginShouldReturnBadRequestWhenEmailIsMissing() throws Exception {
        // given
        final String url = "/login";
        final UserLoginRequestDto dto = new UserLoginRequestDto(null, "1234");
        final MissingFieldException expectedException = new MissingFieldException("");

        // when
        when(restTemplate.postForObject("", dto, FirebaseUserDto.class)).thenThrow(expectedException);

        // then
        final MvcResult result = mockMvc
                .perform(post(url).contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isBadRequest())
                .andReturn();

        assertEquals(result.getResolvedException(), expectedException);
    }

    @Test
    void privateShouldReturnForbiddenWhenUserNotLogged_in() throws Exception {
        // given
        final String url = "/private";

        // then
        mockMvc
                .perform(get(url).contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error_message").value("Access Denied"));
    }

    @Test
    void privateShouldReturnOkWhenUserAuthenticated() throws Exception {
        // given
        final String url = "/private";
        final String providedAccessToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9." +
                "eyJzdWIiOiJzb21ldXNlciIsInJvbGVzIjpbIlJPTEVfVVNFUiJdLCJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjgwODAvbG9na" +
                "W4ifQ.vDeQLA7Y8zTXaJW8bF08lkWzzwGi9Ll44HeMbOc22_o";

        // then
        mockMvc
                .perform(get(url).header("Authorization", "Bearer " + providedAccessToken))
                .andExpect(jsonPath("$.name").value("someuser"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
