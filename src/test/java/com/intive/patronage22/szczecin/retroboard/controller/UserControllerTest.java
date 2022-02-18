package com.intive.patronage22.szczecin.retroboard.controller;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intive.patronage22.szczecin.retroboard.configuration.security.SecurityConfig;
import com.intive.patronage22.szczecin.retroboard.exception.UserAlreadyExistException;
import com.intive.patronage22.szczecin.retroboard.service.UserService;
import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
    private InMemoryUserDetailsManager inMemoryUserDetailsManager;

    @MockBean
    private UserService userService;

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

    @Test
    void loginShouldReturnAccessTokenWhenUserCredentialsAreCorrect() throws Exception {
        // given
        final String url = "/login";
        final String username = "someuser";
        final String password = "1234";

        final UserDetails existingUser = User
                .withUsername(username)
                .password("$2a$10$A0IKJqSv.cSqXb7BuIPw4.GvP1U3VPUIRvkAigPVr6HipH.R3nGLO")
                .roles("USER")
                .build();

        // when
        when(inMemoryUserDetailsManager.loadUserByUsername(username)).thenReturn(existingUser);

        // then
        final String jsonResult = mockMvc
                .perform(post(url)
                        .param("username", username)
                        .param("password", password)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(jsonPath("$.access_token").value("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9."
                        + "eyJzdWIiOiJzb21ldXNlciIsInJvbGVzIjpbIlJPTEVfVVNFUiJdLCJpc3MiOiJodHRwOi8vbG9jYWxob3N0L2xvZ2l"
                        + "uIn0.7Kz-x09Xmaw0qb8FVSzIH9lxMO1_5FHSs1GbJHcsP0o"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        final TypeReference<Map<String, String>> tr = new TypeReference<>() {
        };
        final Map<String, String> map = objectMapper.readValue(jsonResult, tr);
        final String token = map.get("access_token");

        final JWTVerifier verifier = JWT.require(Algorithm.HMAC256(jwtSecret.getBytes())).build();
        final DecodedJWT decodedJwt = verifier.verify(token);

        assertEquals(username, decodedJwt.getSubject());
    }

    @Test
    void loginShouldReturnUnauthorizedWhenUserNotFound() throws Exception {
        // given
        final String url = "/login";
        final String username = "someuser";
        final String password = "1234";
        final UsernameNotFoundException expectedException = new UsernameNotFoundException(username);

        // when
        when(inMemoryUserDetailsManager.loadUserByUsername(username)).thenThrow(expectedException);

        // then
        mockMvc
                .perform(post(url)
                        .param("username", username)
                        .param("password", password)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
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
