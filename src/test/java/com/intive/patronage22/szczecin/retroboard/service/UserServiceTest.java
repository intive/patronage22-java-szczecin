package com.intive.patronage22.szczecin.retroboard.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.intive.patronage22.szczecin.retroboard.exception.DisplayNameFormatException;
import com.intive.patronage22.szczecin.retroboard.exception.EmailFormatException;
import com.intive.patronage22.szczecin.retroboard.exception.PasswordFormatException;
import com.intive.patronage22.szczecin.retroboard.exception.UserAlreadyExistException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {UserService.class})
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private FirebaseAuth firebaseAuth;

    @Test
    public void shouldRegisterAndReturnUserWithErasedCredentialsWhenEmailIsNotTaken() throws FirebaseAuthException {
        // given
        final String email = "test22@test.com";
        final String password = "123456";
        final String displayName = "someuser";
        final String encodedPassword = "$2a$10$svvZN3tiBna47JHfN8AptuOSqbzUlLfAuY9ollfC1J3.CcC2yVH6y";

        final UserRecord userRecord = mock(UserRecord.class);

        // when
        when(firebaseAuth.getUserByEmail(email)).thenThrow(FirebaseAuthException.class);
        when(userRecord.getEmail()).thenReturn(email);
        when(firebaseAuth.createUser(any())).thenReturn(userRecord);
        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);

        // then
        final UserDetails returnedUser = userService.register(email, password, displayName);

        assertNull(returnedUser.getPassword());
        assertEquals(returnedUser.getUsername(), email);
    }

    @Test
    public void shouldThrowUserAlreadyExistsExceptionWhenEmailIsTaken() throws FirebaseAuthException {
        // given
        final String email = "test22@test.com";
        final String password = "123456";
        final String displayName = "someuser";

        final UserRecord userRecord = mock(UserRecord.class);

        // when
        when(firebaseAuth.getUserByEmail(email)).thenReturn(userRecord);

        // then
        assertThrows(UserAlreadyExistException.class, () -> userService.register(email, password, displayName));
    }

    @Test
    public void shouldThrowEmailFormatExceptionWhenEmailIsNotValid() throws FirebaseAuthException {
        // given
        final String email = "test22@.com";
        final String password = "123456";
        final String displayName = "someuser";

        // when
        when(firebaseAuth.getUserByEmail(email)).thenThrow(FirebaseAuthException.class);

        // then
        assertThrows(EmailFormatException.class, () -> userService.register(email, password, displayName));
    }

    @Test
    public void shouldThrowPasswordFormatExceptionWhenPasswordIsNotValid() throws FirebaseAuthException {
        // given
        final String email = "test22@test.com";
        final String password = "12";
        final String displayName = "someuser";

        // when
        when(firebaseAuth.getUserByEmail(email)).thenThrow(FirebaseAuthException.class);

        // then
        assertThrows(PasswordFormatException.class, () -> userService.register(email, password, displayName));
    }

    @Test
    public void shouldThrowDisplayNameFormatExceptionWhenDisplayNameIsNotValid() throws FirebaseAuthException {
        // given
        final String email = "test22@test.com";
        final String password = "123456";
        final String displayName = "";

        // when
        when(firebaseAuth.getUserByEmail(email)).thenThrow(FirebaseAuthException.class);

        // then
        assertThrows(DisplayNameFormatException.class, () -> userService.register(email, password, displayName));
    }
}
