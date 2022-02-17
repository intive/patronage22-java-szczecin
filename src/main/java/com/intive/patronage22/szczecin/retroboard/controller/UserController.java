package com.intive.patronage22.szczecin.retroboard.controller;

import com.google.firebase.auth.FirebaseAuthException;
import com.intive.patronage22.szczecin.retroboard.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequiredArgsConstructor
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class UserController {

    private final UserService userService;

    @GetMapping("/private")
    Authentication privateOp() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    @PostMapping(value = "/register")
    @ResponseStatus(CREATED)
    UserDetails register(@RequestParam final String email, @RequestParam final String password,
                         @RequestParam final String displayName) throws FirebaseAuthException {

        return userService.register(email, password, displayName);
    }
}
