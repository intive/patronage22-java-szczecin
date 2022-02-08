package com.intive.patronage22.szczecin.retroboard.configuration;

import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class FirebaseConfiguration {

    @Bean
    public FirebaseApp firebaseApp(@Value("${FIREBASE_TYPE}") final String type,
                                   @Value("${FIREBASE_CLIENT_ID}") final String clientId,
                                   @Value("${FIREBASE_CLIENT_EMAIL}") final String clientEmail,
                                   @Value("${FIREBASE_CLIENT_PRIVATE_KEY_ID}") final String clientPrivateKeyId,
                                   @Value("${FIREBASE_CLIENT_PRIVATE_KEY_PKCS8}") final String clientPrivateKeyPkcs8,
                                   @Value("${FIREBASE_PROJECT_ID}") final String projectId) throws IOException {
        final ServiceAccountCredentials serviceAccountCredentials =
                ServiceAccountCredentials.fromPkcs8(clientId, clientEmail, clientPrivateKeyPkcs8, clientPrivateKeyId,
                        null);
        final FirebaseOptions options =
                FirebaseOptions.builder().setProjectId(projectId).setCredentials(serviceAccountCredentials).build();
        return FirebaseApp.initializeApp(options);
    }

    @Bean
    public FirebaseAuth firebaseAuth(final FirebaseApp firebaseApp) {
        return FirebaseAuth.getInstance(firebaseApp);
    }

}


