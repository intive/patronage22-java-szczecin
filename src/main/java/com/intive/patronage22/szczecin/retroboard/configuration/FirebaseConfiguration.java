package com.intive.patronage22.szczecin.retroboard.configuration;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.*;
import java.nio.file.Files;

@Configuration
public class FirebaseConfiguration {

    @Bean
    public FirebaseApp firebaseApp(@Value("${FIREBASE_TYPE}") final String type,
                                   @Value("${FIREBASE_CLIENT_ID}") final String clientId,
                                   @Value("${FIREBASE_CLIENT_EMAIL}") final String clientEmail,
                                   @Value("${FIREBASE_CLIENT_PRIVATE_KEY_ID}") final String clientPrivateKeyId,
                                   @Value("${FIREBASE_CLIENT_PRIVATE_KEY_PKCS8}") final String clientPrivateKeyPkcs8,
                                   @Value("${FIREBASE_PROJECT_ID}") final String projectId) {
        try {
            String credentialsPath = String.valueOf(Files.createTempFile("credentials", ".json"));
            final FileWriter fileWriter = new FileWriter(credentialsPath);
            try (final PrintWriter printWriter = new PrintWriter(new BufferedWriter(fileWriter))) {
                JsonObject json = new JsonObject();
                json.addProperty("type", type);
                json.addProperty("project_id", projectId);
                json.addProperty("private_key_id", clientPrivateKeyId);
                json.addProperty("private_key", clientPrivateKeyPkcs8);
                json.addProperty("client_email", clientEmail);
                json.addProperty("client_id", clientId);
                //replaceAll because apparently during creating json object it's replacing \n in private_key into \\n so key is unable to be used
                final String credentials = json.toString().replaceAll("\\\\\\\\", "\\\\");
                printWriter.println(credentials);
            }

            final FileInputStream serviceAccount = new FileInputStream(credentialsPath);
            final FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount)).build();
            return FirebaseApp.initializeApp(options);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Bean
    public FirebaseAuth firebaseAuth(final FirebaseApp firebaseApp) {
        return FirebaseAuth.getInstance(firebaseApp);
    }

}


