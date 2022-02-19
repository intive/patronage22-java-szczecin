package com.intive.patronage22.szczecin.retroboard.configuration.security;

import com.intive.patronage22.szczecin.retroboard.provider.FirebaseAuthenticationProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestTemplate;

@Configuration
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Primary
    @Bean
    AuthenticationProvider authenticationProvider(final FirebaseAuthenticationProvider firebaseAuthenticationProvider) {
        return firebaseAuthenticationProvider;
    }

    @Bean
    RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    FirebaseAuthenticationProvider firebaseAuthenticationProvider() {
        return new FirebaseAuthenticationProvider();
    }
}
