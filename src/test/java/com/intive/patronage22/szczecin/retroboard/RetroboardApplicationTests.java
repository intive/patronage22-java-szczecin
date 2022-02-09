package com.intive.patronage22.szczecin.retroboard;

import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@SpringBootTest
class RetroboardApplicationTests {

    @Test
    void contextLoads() {
    }

    @Configuration
    public static class FirebaseConfiguration {

        private static final FirebaseOptions TEST_OPTIONS =
                FirebaseOptions.builder().setCredentials(new FirebaseCredentialsMock("sample-token"))
                        .setProjectId("sample-project").build();

        @Bean
        public FirebaseApp firebaseApp() {
            return FirebaseApp.initializeApp(TEST_OPTIONS);
        }

        @Bean
        public FirebaseAuth firebaseAuth(final FirebaseApp firebaseApp) {
            return FirebaseAuth.getInstance(firebaseApp);
        }

        public static class FirebaseCredentialsMock extends GoogleCredentials {

            private String accessToken;
            private Long expirationTime;

            public FirebaseCredentialsMock(final String token) {
                this.accessToken = token;
                this.expirationTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(10);
            }

            public AccessToken refreshToken() {
                return new AccessToken(accessToken, new Date(expirationTime));
            }
        }

    }
}
