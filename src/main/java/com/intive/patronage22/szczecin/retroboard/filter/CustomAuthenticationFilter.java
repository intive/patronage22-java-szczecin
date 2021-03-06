package com.intive.patronage22.szczecin.retroboard.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intive.patronage22.szczecin.retroboard.dto.ErrorResponse;
import com.intive.patronage22.szczecin.retroboard.dto.FirebaseUserDto;
import com.intive.patronage22.szczecin.retroboard.exception.EmailFormatException;
import com.intive.patronage22.szczecin.retroboard.exception.MissingFieldException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.springframework.http.HttpHeaders.*;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RequiredArgsConstructor
public class CustomAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final ObjectMapper objectMapper;
    private final String jwtSecret;

    @Override
    public Authentication attemptAuthentication(final HttpServletRequest request, final HttpServletResponse response)
            throws AuthenticationException {

        final String email = request.getParameter("email");
        final String password = request.getParameter("password");

        if (!EmailValidator.getInstance().isValid(email)) {
            throw new EmailFormatException("Invalid email.");
        }

        if (password == null || password.isBlank()) {
            throw new MissingFieldException("Missing password.");
        }

        final UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(email, password);

        return this.authenticationManager.authenticate(authenticationToken);
    }

    @Override
    protected void successfulAuthentication(final HttpServletRequest request, final HttpServletResponse response,
                                            final FilterChain chain, final Authentication authentication) {

        final FirebaseUserDto userDto = (FirebaseUserDto) authentication.getPrincipal();
        response.addHeader(AUTHORIZATION, "Bearer " + userDto.getIdToken());
        response.addHeader(EXPIRES, userDto.getExpiresIn());
    }

    @Override
    protected void unsuccessfulAuthentication(final HttpServletRequest request, final HttpServletResponse response,
                                              final AuthenticationException failed) throws IOException {

        if (failed instanceof MissingFieldException) {
            response.setStatus(BAD_REQUEST.value());
        } else if (failed instanceof EmailFormatException) {
            response.setStatus(BAD_REQUEST.value());
        } else {
            response.setStatus(UNAUTHORIZED.value());
        }

        simpleJsonBodyWriter(response, failed.getMessage());
    }

    private void simpleJsonBodyWriter(final HttpServletResponse response, final String exceptionMessage)
            throws IOException {

        final var exceptionResponse = new ErrorResponse(exceptionMessage);
        response.setContentType(APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), exceptionResponse);
    }
}
