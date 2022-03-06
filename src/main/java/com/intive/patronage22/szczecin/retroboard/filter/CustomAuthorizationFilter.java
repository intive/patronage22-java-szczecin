package com.intive.patronage22.szczecin.retroboard.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.intive.patronage22.szczecin.retroboard.model.User;
import com.intive.patronage22.szczecin.retroboard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

import static com.intive.patronage22.szczecin.retroboard.configuration.security.WebSecurityConfig.*;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RequiredArgsConstructor
@Component
public class CustomAuthorizationFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;
    private final FirebaseAuth firebaseAuth;
    private final UserRepository userRepository;

    @SuppressWarnings("NullableProblems")
    @Override
    protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response,
                                    final FilterChain filterChain) throws ServletException, IOException {

        if (request.getServletPath().equals(URL_LOGIN) || request.getServletPath().equals(URL_REGISTER)) {
            filterChain.doFilter(request, response);
        } else {
            final String authorizationHeader = request.getHeader(AUTHORIZATION);

            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                try {
                    final String token = authorizationHeader.substring("Bearer ".length());
                    final FirebaseToken firebaseToken = firebaseAuth.verifyIdToken(token);

                    final var authenticationToken =
                            new UsernamePasswordAuthenticationToken(
                                    firebaseToken.getEmail(),
                                    null,
                                    new HashSet<>());

                    final Optional<User> optionalUser = userRepository.findUserByEmail(firebaseToken.getEmail());

                    if(optionalUser.isEmpty()){
                        User user = new User(firebaseToken.getUid(),
                                firebaseToken.getEmail(), firebaseToken.getName(), Set.of());
                        userRepository.save(user);
                        log.info("{} added to database",firebaseToken.getEmail());
                    }

                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);

                    filterChain.doFilter(request, response);

                } catch (final Exception exception) {
                    log.error("Error logging in {}", exception.getMessage());

                    response.setStatus(FORBIDDEN.value());
                    response.setContentType(APPLICATION_JSON_VALUE);

                    final Map<String, String> error = new HashMap<>();
                    error.put("error_message", exception.getMessage());
                    objectMapper.writeValue(response.getOutputStream(), error);
                }
            } else {
                filterChain.doFilter(request, response);
            }
        }
    }
}