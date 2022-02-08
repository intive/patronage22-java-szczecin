package com.intive.patronage22.szczecin.retroboard.controller;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intive.patronage22.szczecin.retroboard.exception.UsernameTakenException;
import com.intive.patronage22.szczecin.retroboard.security.SecurityConfig;
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

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest({SecuredController.class, SecurityConfig.class})
class SecuredControllerTest {

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
    void register_should_return_json_body_with_created_user_data_when_user_not_exist_before() throws Exception {
        // given
        final String url = "/register";
        final String username = "someuser";
        final String password = "1234";

        final UserDetails createdUser = User
                .withUsername(username)
                .password(password)
                .roles("USER")
                .build();

        final User returnedUser = (User) createdUser;
        returnedUser.eraseCredentials();

        // when
        when(userService.register(username, password)).thenReturn(returnedUser);

        // then
        mockMvc
                .perform(post(url)
                        .param("username", username)
                        .param("password", password)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.password").value(IsNull.nullValue()))
                .andExpect(status().isCreated());
    }

    @Test
    void register_should_return_conflict_when_user_exist() throws Exception {
        // given
        final String url = "/register";
        final String username = "someuser";
        final String password = "1234";

        // when
        when(userService.register(username, password)).thenThrow(new UsernameTakenException());

        // then
        mockMvc
                .perform(post(url)
                        .param("username", username)
                        .param("password", password)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isConflict());
    }

    @Test
    void login_should_return_access_token_when_user_credentials_are_correct() throws Exception {
        // given
        final String url = "/login";
        final String username = "someuser";
        final String password = "1234";

        final UserDetails existingUser = User
                .withUsername(username)
                .password("$2a$10$wZDpl69jSN6sYRYGZbno.u6LtQ4DDXkwlursaDszbVR24UrYSSkHO")
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
    void login_should_return_unauthorized_when_user_not_exist() throws Exception {
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
                .andExpect(status().isUnauthorized());
    }

    @Test
    void public_should_return_ok_when_user_anonymous() throws Exception {
        // given
        final String url = "/public";

        // then
        mockMvc.perform(get(url))
                .andExpect(jsonPath("$.principal").value("anonymousUser"))
                .andExpect(status().isOk());
    }

    @Test
    void private_should_return_forbidden_when_user_not_logged_in() throws Exception {
        // given
        final String url = "/private";

        // then
        mockMvc
                .perform(get(url).contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isForbidden());
    }

    @Test
    void private_should_return_ok_when_user_authenticated() throws Exception {
        // given
        final String url = "/private";
        final String providedAccessToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9." +
                "eyJzdWIiOiJzb21ldXNlciIsInJvbGVzIjpbIlJPTEVfVVNFUiJdLCJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjgwODAvbG9na" +
                "W4ifQ.vDeQLA7Y8zTXaJW8bF08lkWzzwGi9Ll44HeMbOc22_o";

        // then
        mockMvc
                .perform(get(url).header("Authorization", "Bearer " + providedAccessToken))
                .andExpect(jsonPath("$.name").value("someuser"))
                .andExpect(status().isOk());
    }
}