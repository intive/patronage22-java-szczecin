package com.intive.patronage22.szczecin.retroboard.controller;

import com.intive.patronage22.szczecin.retroboard.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class SecuredController {

    private final UserService userService;

    @GetMapping("/private")
    Authentication privateOp() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    @GetMapping("/public")
    Authentication publicOp() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    UserDetails register(@RequestParam final String username, @RequestParam final String password) {
        return userService.register(username, password);
    }
}
