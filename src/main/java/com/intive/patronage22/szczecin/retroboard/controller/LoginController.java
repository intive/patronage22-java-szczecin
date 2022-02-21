package com.intive.patronage22.szczecin.retroboard.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequiredArgsConstructor
@RequestMapping(produces = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
public class LoginController {

    @PostMapping("/login")
    @ResponseStatus(CREATED)
    @Operation(summary = "Login in user using provided email and password.",
            responses = {@ApiResponse(responseCode = "200", description = "Successfully logged in."),
                    @ApiResponse(responseCode = "400", description = "Email or password is not valid")})
    public void fakeLogin(@RequestParam("email") final String email, @RequestParam("password") final String password) {
        throw new IllegalStateException("This method shouldn't be called. It's implemented by Spring Security filters. " +
                "The reason for it's creation is to override bug in /login swagger documentation https://github.com/springdoc/springdoc-openapi/issues/827 ");
    }
}
