package com.intive.patronage22.szczecin.retroboard.service;

import com.google.firebase.auth.ExportedUserRecord;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.intive.patronage22.szczecin.retroboard.model.Board;
import com.intive.patronage22.szczecin.retroboard.model.User;
import com.intive.patronage22.szczecin.retroboard.repository.BoardRepository;
import com.intive.patronage22.szczecin.retroboard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@EnableScheduling
@Slf4j
@RequiredArgsConstructor
@Component
public class SynchronizeWithFirebaseScheduler {

    private final UserRepository userRepository;
    private final BoardRepository boardRepository;
    private final FirebaseAuth firebaseAuth;

    @Scheduled(fixedRate = 900000, initialDelay = 60000)
    @Transactional
    public void synchronizeUsers() throws FirebaseAuthException {
        log.info("starting the job");

        final Iterable<ExportedUserRecord> pagination = firebaseAuth
                .listUsers(null, 20)
                .iterateAll();

        final List<ExportedUserRecord> allFirebaseUsers = StreamSupport
                .stream(pagination.spliterator(), false)
                .collect(Collectors.toList());

        final List<String> firebaseUsersEmails = allFirebaseUsers.stream()
                .map(UserRecord::getEmail)
                .collect(Collectors.toList());

        final List<String> alreadyExistingEmails = userRepository.findAllByEmailIn(firebaseUsersEmails)
                .stream()
                .map(User::getEmail)
                .collect(Collectors.toList());

        final List<User> firebaseUsersToCreate = allFirebaseUsers.stream()
                .filter(u -> !alreadyExistingEmails.contains(u.getEmail()))
                .map(u -> User.builder()
                        .email(u.getEmail())
                        .displayName(u.getDisplayName())
                        .uid(u.getUid())
                        .deleted(false)
                        .build())
                .collect(Collectors.toList());

        final List<User> createdUsers = (List<User>) userRepository.saveAll(firebaseUsersToCreate);

        log.info("created users count: {}", createdUsers.size());

        final Predicate<User> canUserBeDeleted = u -> u.getUserBoards().isEmpty();

        final Map<Boolean, List<User>> usersPartition = userRepository.findAllByEmailNotIn(firebaseUsersEmails)
                .stream()
                .collect(Collectors.partitioningBy(canUserBeDeleted));

        final List<User> usersToDeleteInDb = usersPartition.get(true);
        final List<User> usersToDeactivateInDb = usersPartition.get(false);

        deleteUsersByEmail(usersToDeleteInDb);
        deactivateUsersByEmail(usersToDeactivateInDb);

        log.info("finishing the job");
    }

    @Transactional
    private void deleteUsersByEmail(final List<User> usersToDelete) {
        log.info("delete users");

        deleteBoardsCreatedByUsers(usersToDelete);
        userRepository.deleteAll(usersToDelete);

        log.info("deleted users count: {}", usersToDelete.size());
    }

    @Transactional
    private void deactivateUsersByEmail(final List<User> usersToDeactivate) {
        log.info("deactivate users");

        deleteBoardsCreatedByUsers(usersToDeactivate);
        usersToDeactivate.forEach(u -> u.setDeleted(true));
        userRepository.saveAll(usersToDeactivate);

        log.info("deactivated users count: {}", usersToDeactivate.size());
    }

    @Transactional
    private void deleteBoardsCreatedByUsers(final List<User> users) {
        final List<Board> boardsToDelete = users.stream()
                .map(User::getCreatedBoards)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        boardRepository.deleteAll(boardsToDelete);

        log.info("deleted boards count: {}", boardsToDelete.size());
    }
}
