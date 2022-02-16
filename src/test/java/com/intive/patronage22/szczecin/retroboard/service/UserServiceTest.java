package com.intive.patronage22.szczecin.retroboard.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.jupiter.api.Assertions.assertNull;
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

        final UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                .setEmail(email)
                .setEmailVerified(true)
                .setDisplayName(displayName)
                .setPassword(password);

        // when
        when(firebaseAuth.getUserByEmail(email)).thenThrow(FirebaseAuthException.class);

        // TODO cannot create new UserRecord instance
        //when(firebaseAuth.createUser(request)).thenReturn(new UserRecord());
        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);

        // then
        final UserDetails returnedUser = userService.register(email, password, displayName);

        assertNull(returnedUser.getPassword());
    }
}
