package com.intive.patronage22.szczecin.retroboard.service;

import com.intive.patronage22.szczecin.retroboard.exception.UserAlreadyExistException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserService {

    private final InMemoryUserDetailsManager inMemoryUserDetailsManager;
    private final PasswordEncoder passwordEncoder;

    public UserDetails register(final String username, final String password) {
        if (userExists(username)) {
            throw new UserAlreadyExistException();
        }

        final UserDetails preparedUser = User.withUsername(username)
                .password(password)
                .roles("USER")
                .passwordEncoder(passwordEncoder::encode)
                .build();

        inMemoryUserDetailsManager.createUser(preparedUser);

        final User createdUser = (User) inMemoryUserDetailsManager.loadUserByUsername(preparedUser.getUsername());
        createdUser.eraseCredentials();

        return createdUser;
    }

    private boolean userExists(final String username) {
        return inMemoryUserDetailsManager.userExists(username);
    }
}
