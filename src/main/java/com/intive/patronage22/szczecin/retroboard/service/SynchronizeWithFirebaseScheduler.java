package com.intive.patronage22.szczecin.retroboard.service;

import com.google.firebase.auth.ExportedUserRecord;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.intive.patronage22.szczecin.retroboard.model.User;
import com.intive.patronage22.szczecin.retroboard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@EnableScheduling
@Slf4j
@RequiredArgsConstructor
@Component
public class SynchronizeWithFirebaseScheduler {

    private final UserRepository userRepository;
    private final FirebaseAuth firebaseAuth;

    @Scheduled(fixedRate = 900000, initialDelay = 60000)
    public void synchronizeUsers() throws FirebaseAuthException {
        log.info("*** starting the job ***");

        final Iterable<ExportedUserRecord> pagination = firebaseAuth
                .listUsers(null, 20)
                .iterateAll();

        final List<ExportedUserRecord> notSignedInUsersFirebase = StreamSupport
                .stream(pagination.spliterator(), false)
                .filter(u -> u.getUserMetadata().getLastSignInTimestamp() == 0)
                .collect(Collectors.toList());

        final List<String> notSignedInUsersEmails = notSignedInUsersFirebase.stream()
                .map(UserRecord::getEmail)
                .collect(Collectors.toList());

        final List<String> alreadyExistingEmails = userRepository.findAllByEmailIn(notSignedInUsersEmails)
                .stream()
                .map(User::getEmail)
                .collect(Collectors.toList());

        final List<User> firebaseUsersToCreate = notSignedInUsersFirebase.stream()
                .filter(u -> !alreadyExistingEmails.contains(u.getEmail()))
                .map(u -> User.builder()
                        .email(u.getEmail())
                        .displayName(u.getDisplayName())
                        .uid(u.getUid())
                        .build())
                .collect(Collectors.toList());

        final List<User> createdUsers = (List<User>) userRepository.saveAll(firebaseUsersToCreate);

        log.info("Created users count: {}", createdUsers.size());
        log.info("*** finishing the job ***");
    }
}
