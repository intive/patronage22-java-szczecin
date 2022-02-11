package com.intive.patronage22.szczecin.retroboard.service;

import com.intive.patronage22.szczecin.retroboard.exception.UserAlreadyExistException;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = UserService.class)
class UserServiceTest {

    @Autowired
    private UserService userService;

    @MockBean
    private InMemoryUserDetailsManager inMemoryUserDetailsManager;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @Test
    void shouldRegisterIfUsernameExists() {
        // given
        final String username = "someuser";
        final String password = "1234";
        final String encodedPassword = "$2a$10$svvZN3tiBna47JHfN8AptuOSqbzUlLfAuY9ollfC1J3.CcC2yVH6y";

        final UserDetails preparedUser = User.withUsername(username)
                .password(password)
                .roles("USER")
                .build();

        // when
        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);
        when(inMemoryUserDetailsManager.userExists(username)).thenReturn(false);
        when(inMemoryUserDetailsManager.loadUserByUsername(username)).thenReturn(preparedUser);

        // then
        final UserDetails returnedUser = userService.register(username, password);
        assertNotNull(returnedUser);
        assertEquals(returnedUser.getUsername(), preparedUser.getUsername());
    }

    @Test
    void shouldReturnUserWithErasedCredentialsWhenUserIsRegistered() {
        // given
        final String username = "someuser";
        final String password = "1234";
        final String encodedPassword = "$2a$10$svvZN3tiBna47JHfN8AptuOSqbzUlLfAuY9ollfC1J3.CcC2yVH6y";

        final UserDetails preparedUser = User.withUsername(username)
                .password(password)
                .roles("USER")
                .build();

        // when
        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);
        when(inMemoryUserDetailsManager.userExists(username)).thenReturn(false);
        when(inMemoryUserDetailsManager.loadUserByUsername(username)).thenReturn(preparedUser);

        // then
        final UserDetails returnedUser = userService.register(username, password);
        assertNull(returnedUser.getPassword());
    }

    @Test
    void registerShouldThrowWhenUsernameIsTaken() {
        // given
        final String username = "someuser";
        final String password = "1234";
        final String encodedPassword = "$2a$10$svvZN3tiBna47JHfN8AptuOSqbzUlLfAuY9ollfC1J3.CcC2yVH6y";

        // when
        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);
        when(inMemoryUserDetailsManager.userExists(username)).thenReturn(true);

        // then
        assertThrows(UserAlreadyExistException.class, () -> userService.register(username, password));
    }
}