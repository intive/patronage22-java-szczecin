package com.intive.patronage22.szczecin.retroboard.service;

import com.google.firebase.auth.ExportedUserRecord;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.ListUsersPage;
import com.google.firebase.auth.UserMetadata;
import com.google.firebase.auth.UserRecord;
import com.intive.patronage22.szczecin.retroboard.dto.EnumStateDto;
import com.intive.patronage22.szczecin.retroboard.model.Board;
import com.intive.patronage22.szczecin.retroboard.model.User;
import com.intive.patronage22.szczecin.retroboard.repository.BoardRepository;
import com.intive.patronage22.szczecin.retroboard.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {SynchronizeWithFirebaseScheduler.class})
class SynchronizeWithFirebaseSchedulerTest {

    @Autowired
    private SynchronizeWithFirebaseScheduler synchronizeWithFirebaseScheduler;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private BoardRepository boardRepository;

    @MockBean
    private FirebaseAuth firebaseAuth;

    @Test
    void synchronizeUsersShouldNotPersistUserToTheDatabaseWhenThereAreNoUsersInFirebase() throws FirebaseAuthException {
        // given
        final ListUsersPage listUsersPage = mock(ListUsersPage.class);
        final List<ExportedUserRecord> firebaseUsers = Collections.emptyList();

        // when
        when(firebaseAuth.listUsers(null, 20)).thenReturn(listUsersPage);
        when(listUsersPage.iterateAll()).thenReturn(firebaseUsers);
        when(userRepository.findAllByEmailIn(any())).thenReturn(Collections.emptyList());

        // then
        synchronizeWithFirebaseScheduler.synchronizeUsers();

        @SuppressWarnings("unchecked") final ArgumentCaptor<List<User>> captor = ArgumentCaptor.forClass(List.class);
        verify(userRepository, times(2)).saveAll(captor.capture());

        final List<String> capturedEmails = captor.getValue().stream().map(User::getEmail).collect(Collectors.toList());
        assertEquals(0, capturedEmails.size());
    }

    @Test
    void synchronizeUsersShouldNotPersistToTheDatabaseWhenThereAreNoNewNotRegisteredUsers() throws FirebaseAuthException {
        // given
        final ListUsersPage listUsersPage = mock(ListUsersPage.class);

        final ExportedUserRecord mockUser1 = mockUser(0L, "uid1", "test1@test.pl", "test1");
        final ExportedUserRecord mockUser2 = mockUser(657521L, "uid2", "test2@test.pl", "test2");
        final ExportedUserRecord mockUser3 = mockUser(654654L, "uid3", "test3@test.pl", "test3");
        final ExportedUserRecord mockUser4 = mockUser(165465465468L, "uid4", "test4@test.pl", "test4");
        final List<ExportedUserRecord> mockUsers = List.of(mockUser1, mockUser2, mockUser3, mockUser4);

        final User userDb1 = createUser("uid1", "test1@test.pl", "test1");
        final User userDb2 = createUser("uid2", "test2@test.pl", "test2");
        final User userDb3 = createUser("uid3", "test3@test.pl", "test3");
        final User userDb4 = createUser("uid4", "test4@test.pl", "test4");
        final List<User> usersDb = List.of(userDb1, userDb2, userDb3, userDb4);

        // when
        when(firebaseAuth.listUsers(null, 20)).thenReturn(listUsersPage);
        when(listUsersPage.iterateAll()).thenReturn(mockUsers);
        when(userRepository.findAllByEmailIn(any())).thenReturn(usersDb);
        when(userRepository.findAllByEmailNotIn(any())).thenReturn(Collections.emptyList());

        // then
        synchronizeWithFirebaseScheduler.synchronizeUsers();

        @SuppressWarnings("unchecked") final ArgumentCaptor<List<User>> usersToSaveCaptor = ArgumentCaptor.forClass(List.class);
        verify(userRepository, times(2)).saveAll(usersToSaveCaptor.capture());

        final List<String> capturedEmails = usersToSaveCaptor.getValue().stream().map(User::getEmail).collect(Collectors.toList());
        assertEquals(0, capturedEmails.size());
    }

    @Test
    @DisplayName("synchronize should persist new registered Users in Firebase to the database - those who have not " +
            "logged in yet and those who have already logged in but don't exist in the database")
    void synchronizeUsersShouldPersistToTheDatabaseNewRegisteredUsers() throws FirebaseAuthException {
        // given
        final ListUsersPage listUsersPage = mock(ListUsersPage.class);

        final ExportedUserRecord mockUser1 = mockUser(0L, "uid1", "test1@test.pl", "test1");
        final ExportedUserRecord mockUser2 = mockUser(0L, "uid2", "test2@test.pl", "test2");
        final ExportedUserRecord mockUser3 = mockUser(0L, "uid3", "test3@test.pl", "test3");
        final ExportedUserRecord mockUser4 = mockUser(165465465468L, "uid3", "test4@test.pl", "test4");
        final List<ExportedUserRecord> mockUsers = List.of(mockUser1, mockUser2, mockUser3, mockUser4);

        // when
        when(firebaseAuth.listUsers(null, 20)).thenReturn(listUsersPage);
        when(listUsersPage.iterateAll()).thenReturn(mockUsers);
        when(userRepository.findAllByEmailIn(any())).thenReturn(Collections.emptyList());
        when(userRepository.findAllByEmailNotIn(any())).thenReturn(Collections.emptyList());

        // then
        synchronizeWithFirebaseScheduler.synchronizeUsers();

        @SuppressWarnings("unchecked") final ArgumentCaptor<List<User>> captor = ArgumentCaptor.forClass(List.class);
        verify(userRepository, times(2)).saveAll(captor.capture());
        final List<String> capturedEmails = captor.getAllValues()
                .stream()
                .flatMap(Collection::stream)
                .map(User::getEmail)
                .collect(Collectors.toList());

        final List<String> firebaseUsersEmails = firebaseUsersEmails(mockUsers);
        assertEquals(firebaseUsersEmails, capturedEmails);
        assertEquals(4, capturedEmails.size());
        assertTrue(capturedEmails.contains("test1@test.pl"));
        assertTrue(capturedEmails.contains("test2@test.pl"));
        assertTrue(capturedEmails.contains("test3@test.pl"));
        assertTrue(capturedEmails.contains("test4@test.pl"));
    }

    @Test
    @DisplayName("synchronize should remove User when he is neither a Board creator nor assigned to the Boards " +
            "and ignore the one that has Boards assigned")
    @SuppressWarnings("unchecked")
    void synchronizeShouldRemoveUserWhenHeIsNeitherABoardCreatorNorAssignedToTheBoards() throws FirebaseAuthException {
        // given
        final ListUsersPage listUsersPage = mock(ListUsersPage.class);
        final ExportedUserRecord firebaseUser = mockUser(0L, "uid1", "test1@test.pl", "test1");
        final List<ExportedUserRecord> firebaseUsers = List.of(firebaseUser);
        final List<String> firebaseUsersEmails = firebaseUsersEmails(firebaseUsers);

        final User userExistingInFirebase = createUser("uid1", "test1@test.pl", "test1");
        final User userDeletedInFirebase = createUser("uid2", "test2@test.pl", "test2");
        final List<User> usersDb = List.of(userExistingInFirebase);

        // when
        when(firebaseAuth.listUsers(null, 20)).thenReturn(listUsersPage);
        when(listUsersPage.iterateAll()).thenReturn(firebaseUsers);
        when(userRepository.findAllByEmailIn(firebaseUsersEmails)).thenReturn(usersDb);
        when(userRepository.findAllByEmailNotIn(firebaseUsersEmails)).thenReturn(List.of(userDeletedInFirebase));

        //then
        synchronizeWithFirebaseScheduler.synchronizeUsers();

        final ArgumentCaptor<List<User>> usersCaptor = ArgumentCaptor.forClass(List.class);
        verify(userRepository, times(2)).saveAll(usersCaptor.capture());
        final List<User> savedUsers = usersCaptor.getValue();
        assertEquals(0, savedUsers.size());

        verify(userRepository).deleteAll(usersCaptor.capture());
        final List<User> deletedUsers = usersCaptor.getValue();
        assertEquals(1, deletedUsers.size());
        assertTrue(deletedUsers.contains(userDeletedInFirebase));

        final ArgumentCaptor<List<Board>> boardCaptor = ArgumentCaptor.forClass(List.class);
        verify(boardRepository, times(2)).deleteAll(boardCaptor.capture());
        final List<Board> deletedBoards = boardCaptor.getAllValues()
                .stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        assertEquals(0, deletedBoards.size());
    }

    @Test
    @DisplayName("synchronize should remove Users and Boards they have created when they have no Boards assigned")
    @SuppressWarnings("unchecked")
    void synchronizeShouldRemoveUserAndBoardHeCreated() throws FirebaseAuthException {
        // given
        final ListUsersPage listUsersPage = mock(ListUsersPage.class);
        final ExportedUserRecord firebaseUser1 = mockUser(0L, "uid1", "test1@test.pl", "test1");
        final ExportedUserRecord firebaseUser2 = mockUser(0L, "uid3", "test3@test.pl", "test3");
        final List<ExportedUserRecord> firebaseUsers = List.of(firebaseUser1, firebaseUser2);
        final List<String> firebaseUsersEmails = firebaseUsersEmails(firebaseUsers);

        final User userExistingInFirebase1 = createUser("uid1", "test1@test.pl", "test1");
        final User userExistingInFirebase2 = createUser("uid3", "test3@test.pl", "test3");
        final List<User> usersDb = List.of(userExistingInFirebase1, userExistingInFirebase2);

        final User userDeletedInFirebase1 = createUser("uid2", "test2@test.pl", "test2");
        final User userDeletedInFirebase2 = createUser("uid4", "test4@test.pl", "test4");
        final Board board1 = TestUtils.buildBoard(1, EnumStateDto.CREATED, userDeletedInFirebase1, new HashSet<>(), 3);
        final Board board2 = TestUtils.buildBoard(2, EnumStateDto.CREATED, userDeletedInFirebase2, new HashSet<>(), 3);
        userDeletedInFirebase1.getCreatedBoards().add(board1);
        userDeletedInFirebase2.getCreatedBoards().add(board2);

        // when
        when(firebaseAuth.listUsers(null, 20)).thenReturn(listUsersPage);
        when(listUsersPage.iterateAll()).thenReturn(firebaseUsers);
        when(userRepository.findAllByEmailIn(firebaseUsersEmails)).thenReturn(usersDb);
        when(userRepository.findAllByEmailNotIn(firebaseUsersEmails))
                .thenReturn(List.of(userDeletedInFirebase1, userDeletedInFirebase2));

        //then
        synchronizeWithFirebaseScheduler.synchronizeUsers();

        final ArgumentCaptor<List<User>> usersCaptor = ArgumentCaptor.forClass(List.class);
        verify(userRepository, times(2)).saveAll(usersCaptor.capture());
        final List<User> capturedUsers = usersCaptor.getValue();
        assertEquals(0, capturedUsers.size());

        verify(userRepository).deleteAll(usersCaptor.capture());
        final List<User> deletedUsers = usersCaptor.getValue();
        assertEquals(2, deletedUsers.size());
        assertTrue(deletedUsers.contains(userDeletedInFirebase1));

        final ArgumentCaptor<List<Board>> boardCaptor = ArgumentCaptor.forClass(List.class);
        verify(boardRepository, times(2)).deleteAll(boardCaptor.capture());
        final List<Board> deletedBoards = boardCaptor.getAllValues()
                .stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        assertEquals(2, deletedBoards.size());
        assertTrue(deletedBoards.contains(board1));
        assertTrue(deletedBoards.contains(board2));
    }

    @Test
    @DisplayName("synchronize should deactivate User when he is assigned to the Board and delete the Boards he created")
    @SuppressWarnings("unchecked")
    void synchronizeShouldDeactivateUserAndDeleteBoard() throws FirebaseAuthException {
        // given
        final ListUsersPage listUsersPage = mock(ListUsersPage.class);
        final ExportedUserRecord firebaseUser1 = mockUser(0L, "uid1", "test1@test.pl", "test1");
        final ExportedUserRecord firebaseUser2 = mockUser(0L, "uid3", "test3@test.pl", "test3");
        final List<ExportedUserRecord> firebaseUsers = List.of(firebaseUser1, firebaseUser2);
        final List<String> firebaseUsersEmails = firebaseUsersEmails(firebaseUsers);

        final User userExistingInFirebase1 = createUser("uid1", "test1@test.pl", "test1");
        final User userExistingInFirebase2 = createUser("uid3", "test3@test.pl", "test3");
        final List<User> usersDb = List.of(userExistingInFirebase1, userExistingInFirebase2);

        final User userDeletedInFirebase1 = createUser("uid2", "test2@test.pl", "test2");
        final User boardOwner = createUser("uid4", "test4@test.pl", "test4");
        final Board board1 = TestUtils.buildBoard(1, EnumStateDto.CREATED, boardOwner, new HashSet<>(), 3);
        final Board board2 = TestUtils.buildBoard(2, EnumStateDto.CREATED, userDeletedInFirebase1, new HashSet<>(), 3);
        userDeletedInFirebase1.getUserBoards().add(board1);
        userDeletedInFirebase1.getCreatedBoards().add(board2);

        // when
        when(firebaseAuth.listUsers(null, 20)).thenReturn(listUsersPage);
        when(listUsersPage.iterateAll()).thenReturn(firebaseUsers);
        when(userRepository.findAllByEmailIn(firebaseUsersEmails)).thenReturn(usersDb);
        when(userRepository.findAllByEmailNotIn(firebaseUsersEmails)).thenReturn(List.of(userDeletedInFirebase1));

        //then
        synchronizeWithFirebaseScheduler.synchronizeUsers();

        final ArgumentCaptor<List<User>> usersCaptor = ArgumentCaptor.forClass(List.class);
        verify(userRepository, times(2)).saveAll(usersCaptor.capture());
        final List<User> updatedUsers = usersCaptor.getAllValues()
                .stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        assertEquals(1, updatedUsers.size());
        assertTrue(updatedUsers.get(0).isDeleted());

        verify(userRepository).deleteAll(usersCaptor.capture());
        final List<User> deletedUsers = usersCaptor.getValue();
        assertEquals(0, deletedUsers.size());
        assertFalse(deletedUsers.contains(userDeletedInFirebase1));

        final ArgumentCaptor<List<Board>> boardCaptor = ArgumentCaptor.forClass(List.class);
        verify(boardRepository, times(2)).deleteAll(boardCaptor.capture());
        final List<Board> deletedBoards = boardCaptor.getAllValues()
                .stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        assertEquals(1, deletedBoards.size());
        assertTrue(deletedBoards.contains(board2));
        assertFalse(deletedBoards.contains(board1));
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

    private User createUser(final String uid, final String email, final String displayName) {
        return User.builder()
                .uid(uid)
                .email(email)
                .displayName(displayName)
                .deleted(false)
                .userBoards(new HashSet<>())
                .createdBoards(new HashSet<>())
                .build();
    }

    private List<String> firebaseUsersEmails(final List<ExportedUserRecord> fbUsers) {
        return fbUsers.stream().map(UserRecord::getEmail).collect(Collectors.toList());
    }
}