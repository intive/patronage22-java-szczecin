package com.intive.patronage22.szczecin.retroboard.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intive.patronage22.szczecin.retroboard.dto.FirebaseUserDto;
import com.intive.patronage22.szczecin.retroboard.exception.MissingFieldException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

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
        
        if (email == null || email.isBlank())
            throw new MissingFieldException("Missing email.");
        if (password == null || password.isBlank())
            throw new MissingFieldException("Missing password.");

        final UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(email, password);

        return this.authenticationManager.authenticate(authenticationToken);
    }

    @Override
    protected void successfulAuthentication(final HttpServletRequest request, final HttpServletResponse response,
                                            final FilterChain chain, final Authentication authentication)
            throws IOException {
        
        final FirebaseUserDto userDto = (FirebaseUserDto)authentication.getPrincipal();
        response.addHeader("Authorization", "Bearer " + userDto.getIdToken());
        response.addHeader("Expires", userDto.getExpiresIn());
    }

    @Override
    protected void unsuccessfulAuthentication(final HttpServletRequest request, final HttpServletResponse response,
                                              final AuthenticationException failed) throws IOException {

        if (failed instanceof MissingFieldException)
            response.setStatus(BAD_REQUEST.value());
        else
            response.setStatus(UNAUTHORIZED.value());
        simpleJsonBodyWriter(response, "error_message", failed.getMessage());
    }

    private void simpleJsonBodyWriter(final HttpServletResponse response, final String key, final String value)
            throws IOException {

        final Map<String, String> tokens = Map.of(key, value);
        response.setContentType(APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), tokens);
    }
}
