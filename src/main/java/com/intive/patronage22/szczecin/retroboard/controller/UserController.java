package com.intive.patronage22.szczecin.retroboard.controller;

import com.google.firebase.auth.FirebaseAuthException;
import com.intive.patronage22.szczecin.retroboard.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@Validated
@RequiredArgsConstructor
@Slf4j
@RequestMapping(value = "${retroboard.api-version}", produces = MediaType.APPLICATION_JSON_VALUE)
public class UserController {

    private final UserService userService;

    @GetMapping("/private")
    @Operation(security = @SecurityRequirement(name = "tokenAuth"))
    Authentication privateOp() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    @PostMapping(value = "/register")
    @ResponseStatus(CREATED)
    @Operation(summary = "Create user in Firebase.",
            responses = {@ApiResponse(responseCode = "201", description = "User created"),
                    @ApiResponse(responseCode = "409", description = "User already exist"),
                    @ApiResponse(responseCode = "400", description = "Email, displayName or password not valid")})
    UserDetails register(@RequestParam @Email @Size(max = 64) @NotEmpty final String email,
                         @RequestParam @Size(min = 6, max = 64) @NotBlank @NotEmpty final String password,
                         @RequestParam @Size(min = 4, max = 64) @NotBlank @NotEmpty final String displayName)
            throws FirebaseAuthException {

        return userService.register(email, password, displayName);
    }

    @PostMapping(value = "/login", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    @ResponseStatus(CREATED)
    @Operation(summary = "Login in user using provided email and password.",
            responses = {@ApiResponse(responseCode = "200", description = "Successfully logged in."),
                    @ApiResponse(responseCode = "400", description = "Email or password is not valid")})
    public void fakeLogin(@RequestParam("email") final String email, @RequestParam("password") final String password) {
        throw new IllegalStateException(
                "This method shouldn't be called. It's implemented by Spring Security filters. " +
                "The reason for it's creation is to override bug in /login swagger documentation " +
                        "https://github.com/springdoc/springdoc-openapi/issues/827 ");
    }
}
