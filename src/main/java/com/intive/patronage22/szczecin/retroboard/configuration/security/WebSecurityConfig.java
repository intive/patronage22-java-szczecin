package com.intive.patronage22.szczecin.retroboard.configuration.security;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.Map;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Slf4j
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    private final CustomAuthorizationFilter customAuthorizationFilter;
    private final ObjectMapper objectMapper;
    private final FirebaseAuthenticationProvider authenticationProvider;

    @Value("${retroboard.jwt.secret}")
    private String jwtSecret;
    @Value("${retroboard.api-version}")
    private String apiVersion;

    @Override
    protected void configure(final AuthenticationManagerBuilder auth){
        auth.authenticationProvider(authenticationProvider);
    }

    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        http.csrf().disable();
        http.sessionManagement().sessionCreationPolicy(STATELESS);
        http.authorizeRequests().antMatchers(apiVersion + "/register",
                apiVersion + "/boards/**", "/swagger-ui/**", "/v3/api-docs/**",
                "/error", "/actuator/health").permitAll();
        http.authorizeRequests().antMatchers("/private").authenticated();
        http.addFilter(new CustomAuthenticationFilter(authenticationManager(), objectMapper, jwtSecret));
        http.authorizeRequests().anyRequest().authenticated();
        http.addFilterBefore(customAuthorizationFilter, UsernamePasswordAuthenticationFilter.class);
        http.headers().frameOptions().disable();

        http.exceptionHandling()
                .authenticationEntryPoint((req, res, exception) -> handleUnauthenticatedAccess(res));
    }

    private void handleUnauthenticatedAccess(final HttpServletResponse response) throws IOException {
        final Map<String, String> tokens = Map.of("error_message", "Access Denied");
        SecurityContextHolder.clearContext();

        response.setContentType(APPLICATION_JSON_VALUE);
        response.setStatus(FORBIDDEN.value());

        objectMapper.writeValue(response.getOutputStream(), tokens);

        log.error("error while logging in. Error code: " + response.getStatus());
    }
}
