package com.intive.patronage22.szczecin.retroboard.provider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intive.patronage22.szczecin.retroboard.dto.UserDto;
import com.intive.patronage22.szczecin.retroboard.dto.UserLoginRequestDto;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

@Component
@NoArgsConstructor
public class FirebaseAuthenticationProvider implements AuthenticationProvider {

    @Value("${FIREBASE_API_KEY}")
    private String apiKey;

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        final RestTemplate restTemplate = new RestTemplate();
        final String email = authentication.getPrincipal().toString();
        final String password = authentication.getCredentials().toString();
        try {
            UserDto userDto = restTemplate.postForObject(
                    "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=" + apiKey,
                    new UserLoginRequestDto(email, password), UserDto.class);
            return new UsernamePasswordAuthenticationToken(userDto, password, new HashSet<>());
        } catch (RestClientResponseException e) {
            try {
                Map<String, Map<String, Object>> result = new ObjectMapper()
                        .readValue(e.getResponseBodyAsString(), HashMap.class);
                String msg = (String)result.get("error").get("message");
                switch (msg) {
                    case "EMAIL_NOT_FOUND":
                        throw new UsernameNotFoundException("Email not found.");
                    case "INVALID_PASSWORD":
                        throw new BadCredentialsException("Invalid password.");
                    case "USER_DISABLED":
                        throw new DisabledException("The user account has been disabled by an administrator.");
                    default:
                        throw e;
                }
            } catch(JsonProcessingException e1) {
                throw e;
            }
        }
    }
}