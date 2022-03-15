package com.intive.patronage22.szczecin.retroboard.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.intive.patronage22.szczecin.retroboard.exception.UserAlreadyExistException;
import com.intive.patronage22.szczecin.retroboard.model.User;
import com.intive.patronage22.szczecin.retroboard.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {UserService.class})
class UserServiceTest {

    @Autowired
    private UserService userService;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private FirebaseAuth firebaseAuth;

    @Test
    void registerShouldReturnUserWithErasedCredentialsWhenEmailIsNotTaken() throws FirebaseAuthException {
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
    void registerShouldThrowUserAlreadyExistsExceptionWhenEmailIsTaken() throws FirebaseAuthException {
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
    void searchShouldReturnListWithEmailsWhenEmailIsOk() {
        // given
        final String providedEmail = "test";
        final List<User> users = List.of(
                new User("dsadasdasdas", "test12@plo.com", "yytS", Set.of(), Set.of()),
                new User("dsada212ddas", "sodttest2@tyk.pl", "dscS", Set.of(), Set.of()),
                new User("dsDeXVBasdas", "sodniktest@sok.com", "y32S", Set.of(), Set.of())
        );

        // when
        when(userRepository.findAllByEmailContaining(providedEmail)).thenReturn(users);
        final List<String> search = userService.search(providedEmail);

        // then
        assertEquals(3, users.size());
        assertEquals(List.of("test12@plo.com", "sodttest2@tyk.pl", "sodniktest@sok.com"), search);
    }
}
