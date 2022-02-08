package com.intive.patronage22.szczecin.retroboard.service;

import com.intive.patronage22.szczecin.retroboard.exception.UsernameTakenException;
import org.junit.Assert;
import org.junit.Test;
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
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @MockBean
    private InMemoryUserDetailsManager inMemoryUserDetailsManager;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @Test
    public void should_register_if_username_exists() {
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
        Assert.assertNotNull(returnedUser);
        assertEquals(returnedUser.getUsername(), preparedUser.getUsername());
    }

    @Test
    public void should_return_user_with_erased_credentials_when_user_is_registered() {
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
    public void register_should_throw_when_username_is_taken() {
        // given
        final String username = "someuser";
        final String password = "1234";
        final String encodedPassword = "$2a$10$svvZN3tiBna47JHfN8AptuOSqbzUlLfAuY9ollfC1J3.CcC2yVH6y";

        // when
        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);
        when(inMemoryUserDetailsManager.userExists(username)).thenReturn(true);

        // then
        assertThrows(UsernameTakenException.class, () -> userService.register(username, password));
    }
}