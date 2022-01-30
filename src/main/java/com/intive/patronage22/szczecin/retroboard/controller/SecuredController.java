package com.intive.patronage22.szczecin.retroboard.controller;

import com.intive.patronage22.szczecin.retroboard.exception.UsernameTakenException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class SecuredController {

    private final InMemoryUserDetailsManager inMemoryUserDetailsManager;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/private")
    Authentication privateOp() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    @GetMapping("/public")
    Authentication publicOp() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/register")
    UserDetails register(@RequestParam String username, @RequestParam String password) {
        UserDetails preparedUser = User.withUsername(username)
                .password(password)
                .roles("USER")
                .passwordEncoder(passwordEncoder::encode)
                .build();

        if (inMemoryUserDetailsManager.userExists(username)) {
            throw new UsernameTakenException();
        }

        inMemoryUserDetailsManager.createUser(preparedUser);

        User createdUser = (User) inMemoryUserDetailsManager.loadUserByUsername(preparedUser.getUsername());
        createdUser.eraseCredentials();

        return createdUser;
    }
}
