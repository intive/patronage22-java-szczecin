package com.intive.patronage22.szczecin.retroboard.configuration;

import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@TestConfiguration
public class FirebaseTestConfiguration {

    private static final FirebaseOptions TEST_OPTIONS =
            FirebaseOptions.builder().setCredentials(new FirebaseCredentialsMock("sample-token"))
                    .setProjectId("sample-project").build();

    @Bean
    public FirebaseApp firebaseApp() {
        final FirebaseApp firebaseApp = FirebaseApp.getApps()
                .stream()
                .filter(app -> app.getName().equals(FirebaseApp.DEFAULT_APP_NAME))
                .findAny()
                .orElse(null);
        if (firebaseApp != null)
            return firebaseApp;
        return FirebaseApp.initializeApp(TEST_OPTIONS);
    }

    @Bean
    public FirebaseAuth firebaseAuth(final FirebaseApp firebaseApp) {
        return FirebaseAuth.getInstance(firebaseApp);
    }

    public static class FirebaseCredentialsMock extends GoogleCredentials {

        private final String accessToken;
        private final Long expirationTime;

        public FirebaseCredentialsMock(final String token) {
            this.accessToken = token;
            this.expirationTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(10);
        }

        @Override
        public AccessToken refreshAccessToken() {
            return new AccessToken(accessToken, new Date(expirationTime));
        }
    }
}
