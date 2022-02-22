package com.intive.patronage22.szczecin.retroboard.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.intive.patronage22.szczecin.retroboard.exception.UserAlreadyExistException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final PasswordEncoder passwordEncoder;
    private final FirebaseAuth firebaseAuth;

    public UserDetails register(final String email, final String password, final String displayName)
            throws FirebaseAuthException {

        if (isUserExist(email)) {
            throw new UserAlreadyExistException("User already exist");
        }

        final UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                .setEmail(email)
                .setEmailVerified(true)
                .setDisplayName(displayName)
                .setPassword(password);

        final UserRecord createdUser = firebaseAuth.createUser(request);

        final User springUser = (User) User.withUsername(createdUser.getEmail())
                .password(password)
                .roles("USER")
                .passwordEncoder(passwordEncoder::encode)
                .build();

        springUser.eraseCredentials();
        
        return springUser;
    }

    public boolean isUserExist(final String email) {
        try {
            firebaseAuth.getUserByEmail(email);
            return true;
        } catch (final FirebaseAuthException e) {
            return false;
        }
    }
}
