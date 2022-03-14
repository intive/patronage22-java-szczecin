package com.intive.patronage22.szczecin.retroboard.configuration.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intive.patronage22.szczecin.retroboard.dto.ErrorResponse;
import com.intive.patronage22.szczecin.retroboard.filter.CustomAuthenticationFilter;
import com.intive.patronage22.szczecin.retroboard.filter.CustomAuthorizationFilter;
import com.intive.patronage22.szczecin.retroboard.provider.FirebaseAuthenticationProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Slf4j
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    public static final String URL_LOGIN = "/api/v1/login";
    public static final String URL_REGISTER = "/api/v1/register";

    private final CustomAuthorizationFilter customAuthorizationFilter;
    private final ObjectMapper objectMapper;
    private final FirebaseAuthenticationProvider authenticationProvider;

    @Value("${retroboard.jwt.secret}")
    private String jwtSecret;

    @Override
    protected void configure(final AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(authenticationProvider);
    }

    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        http.csrf().disable();
        http.sessionManagement().sessionCreationPolicy(STATELESS);
        http.authorizeRequests().antMatchers(URL_REGISTER, URL_LOGIN,
                "/swagger-ui/**", "/v3/api-docs/**", "/error", "/actuator/health").permitAll();

        http.addFilter(getCustomAuthenticationFilter());
        http.authorizeRequests().anyRequest().authenticated();
        http.addFilterBefore(customAuthorizationFilter, UsernamePasswordAuthenticationFilter.class);
        http.headers().frameOptions().disable();

        http.exceptionHandling()
                .authenticationEntryPoint((req, res, exception) -> handleUnauthenticatedAccess(res));
    }

    private void handleUnauthenticatedAccess(final HttpServletResponse response) throws IOException {
        final var errorResponse = new ErrorResponse("Access Denied");
        SecurityContextHolder.clearContext();

        response.setContentType(APPLICATION_JSON_VALUE);
        response.setStatus(FORBIDDEN.value());

        objectMapper.writeValue(response.getOutputStream(), errorResponse);

        log.error("error while logging in. Error code: " + response.getStatus());
    }

    private CustomAuthenticationFilter getCustomAuthenticationFilter() throws Exception {
        final var filter = new CustomAuthenticationFilter(authenticationManager(), objectMapper, jwtSecret);
        filter.setFilterProcessesUrl(URL_LOGIN);
        return filter;
    }
}