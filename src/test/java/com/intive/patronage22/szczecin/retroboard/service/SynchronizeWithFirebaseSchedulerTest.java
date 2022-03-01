package com.intive.patronage22.szczecin.retroboard.service;

import com.google.firebase.auth.ExportedUserRecord;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.ListUsersPage;
import com.google.firebase.auth.UserMetadata;
import com.google.firebase.auth.UserRecord;
import com.intive.patronage22.szczecin.retroboard.model.User;
import com.intive.patronage22.szczecin.retroboard.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {SynchronizeWithFirebaseScheduler.class})
class SynchronizeWithFirebaseSchedulerTest {

    @Autowired
    private SynchronizeWithFirebaseScheduler synchronizeWithFirebaseScheduler;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private FirebaseAuth firebaseAuth;

    @Test
    void synchronizeUsersShouldNotPersistToTheDatabaseWhenThereIsNoNewNotSignedInUsers() throws FirebaseAuthException {
        // given
        final ListUsersPage listUsersPage = mock(ListUsersPage.class);
        final List<ExportedUserRecord> alreadyExistingUsers = new ArrayList<>();

        // when
        when(firebaseAuth.listUsers(null, 20)).thenReturn(listUsersPage);
        when(listUsersPage.iterateAll()).thenReturn(alreadyExistingUsers);
        when(userRepository.findAllByEmailIn(any())).thenReturn(Collections.emptyList());

        // then
        synchronizeWithFirebaseScheduler.synchronizeUsers();

        @SuppressWarnings("unchecked") final ArgumentCaptor<List<User>> captor = ArgumentCaptor.forClass(List.class);
        verify(userRepository).saveAll(captor.capture());

        final List<String> capturedEmails = captor.getValue().stream().map(User::getEmail).collect(Collectors.toList());

        final List<String> expectedUsersEmails = firebaseUsersEmails(alreadyExistingUsers);
        assertEquals(expectedUsersEmails, capturedEmails);
        assertEquals(0, capturedEmails.size());
    }

    @Test
    void synchronizeUsersShouldPersistToTheDatabaseWhenThereAreNewNotSignedInUsers() throws FirebaseAuthException {
        // given
        final ListUsersPage listUsersPage = mock(ListUsersPage.class);

        final ExportedUserRecord mockUser1 = mockUser(0L, "uid1", "test1@test.pl", "test1");
        final ExportedUserRecord mockUser2 = mockUser(0L, "uid2", "test2@test.pl", "test2");
        final ExportedUserRecord mockUser3 = mockUser(0L, "uid3", "test3@test.pl", "test3");
        final List<ExportedUserRecord> mockUsers = List.of(mockUser1, mockUser2, mockUser3);

        // when
        when(firebaseAuth.listUsers(null, 20)).thenReturn(listUsersPage);
        when(listUsersPage.iterateAll()).thenReturn(mockUsers);
        when(userRepository.findAllByEmailIn(any())).thenReturn(Collections.emptyList());

        // then
        synchronizeWithFirebaseScheduler.synchronizeUsers();

        @SuppressWarnings("unchecked") final ArgumentCaptor<List<User>> captor = ArgumentCaptor.forClass(List.class);
        verify(userRepository).saveAll(captor.capture());
        final List<String> capturedEmails = captor.getValue().stream().map(User::getEmail).collect(Collectors.toList());

        final List<String> expectedUsersEmails = firebaseUsersEmails(mockUsers);
        assertEquals(expectedUsersEmails, capturedEmails);
        assertEquals(3, capturedEmails.size());
    }

    private ExportedUserRecord mockUser(final long lastSignInTimestamp, final String uid, final String email,
                                        final String displayName) {

        final ExportedUserRecord exportedUserRecord = mock(ExportedUserRecord.class);

        final UserMetadata userMetadata = mock(UserMetadata.class);
        when(userMetadata.getLastSignInTimestamp()).thenReturn(lastSignInTimestamp);

        when(exportedUserRecord.getUserMetadata()).thenReturn(userMetadata);
        when(exportedUserRecord.getUid()).thenReturn(uid);
        when(exportedUserRecord.getEmail()).thenReturn(email);
        when(exportedUserRecord.getDisplayName()).thenReturn(displayName);

        return exportedUserRecord;
    }

    private List<String> firebaseUsersEmails(final List<ExportedUserRecord> fbUsers) {
        return fbUsers.stream().map(UserRecord::getEmail).collect(Collectors.toList());
    }
}