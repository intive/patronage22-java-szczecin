package com.intive.patronage22.szczecin.retroboard.service;

import com.intive.patronage22.szczecin.retroboard.dto.*;
import com.intive.patronage22.szczecin.retroboard.exception.BadRequestException;
import com.intive.patronage22.szczecin.retroboard.exception.NotFoundException;
import com.intive.patronage22.szczecin.retroboard.model.*;
import com.intive.patronage22.szczecin.retroboard.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = BoardCardService.class)
class BoardCardServiceTest {

    @Autowired
    private BoardCardService boardCardService;

    @MockBean
    private BoardCardsRepository boardCardsRepository;

    @MockBean
    private BoardRepository boardRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private BoardCardsVotesRepository boardCardsVotesRepository;

    @MockBean
    private BoardCardsActionsRepository boardCardsActionsRepository;

    private static Stream<Arguments> provideInputsForBoardCardsColumnValidation() {
        return Stream.of(
                Arguments.of(BoardCardsColumn.SUCCESS, "0"),
                Arguments.of(BoardCardsColumn.FAILURES, "1"),
                Arguments.of(BoardCardsColumn.KUDOS, "2")
        );
    }

    @ParameterizedTest
    @MethodSource("provideInputsForBoardCardsColumnValidation")
    void createBoardCardShouldReturnBoardCardDto(final BoardCardsColumn boardCardsColumn, final String orderNumber) {
        // given
        final Integer boardId = 1;
        final BoardCardDto requestDto = BoardCardDto.builder()
                .cardText("Some valid cardText test")
                .columnId(Integer.valueOf(orderNumber))
                .build();
        final String email = "test22@test.com";
        final String uid = "1234";
        final User user = new User(uid, email, "john14", false, Set.of(), Set.of());
        final Board board = buildBoard(boardId, EnumStateDto.CREATED, 5, user, Set.of(), Set.of());

        //when
        when(userRepository.findUserByEmail(email)).thenReturn(Optional.of(user));
        when(boardRepository.existsById(boardId)).thenReturn(true);
        when(boardRepository.findBoardByIdAndCreatorOrAssignedUser(boardId, user)).thenReturn(Optional.of(board));

        //then
        final BoardCardDto responseDto = boardCardService
                .createBoardCard(requestDto, boardId, email);

        final ArgumentCaptor<BoardCard> boardCardCaptor = ArgumentCaptor.forClass(BoardCard.class);
        Mockito.verify(boardCardsRepository).save(boardCardCaptor.capture());

        final BoardCard savedBoardCard = boardCardCaptor.getValue();

        assertEquals(responseDto.getCardText(), savedBoardCard.getText());
        assertEquals(responseDto.getCardText(), requestDto.getCardText());
        assertEquals(responseDto.getColumnId(), savedBoardCard.getColumn().getColumnId());
        assertEquals(responseDto.getColumnId(), requestDto.getColumnId());
        assertEquals(boardCardsColumn, savedBoardCard.getColumn());
        assertEquals(responseDto.getBoardCardCreator(), savedBoardCard.getCreator().getEmail());
        assertEquals(responseDto.getBoardCardCreator(), email);
    }

    @Test
    void createBoardCardShouldThrowBadRequestExceptionWhenUserIsNotFound() {
        // given
        final Integer boardId = 1;
        final BoardCardDto requestDto = BoardCardDto.builder()
                .cardText("Some valid cardText test")
                .columnId(0)
                .build();
        final String email = "test22@test.com";

        //when
        when(userRepository.findUserByEmail(email)).thenThrow(BadRequestException.class);

        //then
        assertThrows(BadRequestException.class, () -> boardCardService
                .createBoardCard(requestDto, boardId, email));
    }

    @Test
    void createBoardCardShouldThrowNotFoundExceptionWhenBoardIsNotFound() {
        // given
        final Integer boardId = 1;
        final BoardCardDto requestDto = BoardCardDto.builder()
                .cardText("Some valid cardText test")
                .columnId(0)
                .build();
        final String email = "test22@test.com";
        final String uid = "1234";
        final User user = new User(uid, email, "john14", false, Set.of(), Set.of());

        //when
        when(userRepository.findUserByEmail(email)).thenReturn(Optional.of(user));
        when(boardRepository.findById(boardId)).thenThrow(NotFoundException.class);

        //then
        assertThrows(NotFoundException.class, () -> boardCardService
                .createBoardCard(requestDto, boardId, email));
    }

    @Test
    void createBoardCardShouldThrowBadRequestExceptionWhenUserHasNoAccessToBoard() {
        // given
        final Integer boardId = 1;
        final BoardCardDto requestDto = BoardCardDto.builder()
                .cardText("Some valid cardText test")
                .columnId(0)
                .build();
        final String email = "test22@test.com";
        final String uid = "1234";
        final User user = new User(uid, email, "john14", false, Set.of(), Set.of());

        //when
        when(userRepository.findUserByEmail(email)).thenReturn(Optional.of(user));
        when(boardRepository.existsById(boardId)).thenReturn(true);
        when(boardRepository.findBoardByIdAndCreatorOrAssignedUser(boardId, user)).thenThrow(BadRequestException.class);

        //then
        assertThrows(BadRequestException.class, () -> boardCardService
                .createBoardCard(requestDto, boardId, email));
    }

    @Test
    void createBoardCardShouldThrowBadRequestExceptionWhenBoardStateIsNotCreated() {
        // given
        final Integer boardId = 1;
        final BoardCardDto requestDto = BoardCardDto.builder()
                .cardText("Some valid cardText test")
                .columnId(0)
                .build();
        final String email = "test22@test.com";
        final String uid = "1234";
        final User user = new User(uid, email, "john14", false, Set.of(), Set.of());
        final Board board = buildBoard(boardId, EnumStateDto.VOTING, 5, user, Set.of(), Set.of());

        //when
        when(userRepository.findUserByEmail(email)).thenReturn(Optional.of(user));
        when(boardRepository.existsById(boardId)).thenReturn(true);
        when(boardRepository.findBoardByIdAndCreatorOrAssignedUser(boardId, user)).thenReturn(Optional.of(board));

        //then
        assertThrows(BadRequestException.class, () -> boardCardService.createBoardCard(requestDto, boardId, email));
    }

    @Test
    void removeBoardCardShouldPassWhenUserOwnsBoardCard() {
        // given
        final Integer cardId = 1;
        final String email = "test22@test.com";
        final User user = new User("1234", email, "john14", false, Set.of(), Set.of());
        final User boardOwner = new User("12345", "some@test.com", "test", false, Set.of(), Set.of());
        final Board board = buildBoard(1, EnumStateDto.CREATED, 5, boardOwner, Set.of(), Set.of());
        final BoardCard boardCard = buildBoardCard(cardId, board, BoardCardsColumn.SUCCESS, user, List.of());

        // when
        when(userRepository.findUserByEmail(email)).thenReturn(Optional.of(user));
        when(boardCardsRepository.findById(cardId)).thenReturn(Optional.of(boardCard));

        //then
        boardCardService.removeCard(cardId, email);
        verify(boardCardsRepository).deleteById(cardId);
    }

    @Test
    void removeBoardCardShouldPassWhenUserOwnsBoard() {
        // given
        final Integer cardId = 1;
        final String email = "test22@test.com";
        final User boardOwner = new User("1234", email, "john14", false, Set.of(), Set.of());
        final User user = new User("12345", "some@test.com", "test", false, Set.of(), Set.of());
        final Board board = buildBoard(1, EnumStateDto.CREATED, 5, boardOwner, Set.of(), Set.of());
        final BoardCard boardCard = buildBoardCard(cardId, board, BoardCardsColumn.SUCCESS, user, List.of());

        // when
        when(userRepository.findUserByEmail(email)).thenReturn(Optional.of(boardOwner));
        when(boardCardsRepository.findById(cardId)).thenReturn(Optional.of(boardCard));

        //then
        boardCardService.removeCard(cardId, email);
        verify(boardCardsRepository).deleteById(cardId);
    }

    @Test
    void removeBoardCardShouldThrowBadRequestWhenBoardStateIsNotCreated() {
        // given
        final Integer cardId = 1;
        final String email = "test22@test.com";
        final User user = new User("1234", email, "john14", false, Set.of(), Set.of());
        final User boardOwner = new User("12345", "some@test.com", "test", false, Set.of(), Set.of());
        final Board board = buildBoard(1, EnumStateDto.VOTING, 5, boardOwner, Set.of(), Set.of());
        final BoardCard boardCard = buildBoardCard(cardId, board, BoardCardsColumn.SUCCESS, user, List.of());

        // when
        when(userRepository.findUserByEmail(email)).thenReturn(Optional.of(user));
        when(boardCardsRepository.findById(cardId)).thenReturn(Optional.of(boardCard));

        //then
        assertThrows(BadRequestException.class, () -> boardCardService.removeCard(cardId, email));
    }

    @Test
    void removeBoardCardShouldThrowBadRequestWhenUserIsNotOwner() {
        // given
        final Integer cardId = 1;
        final String email = "test22@test.com";
        final User user = new User("1234", email, "john14", false, Set.of(), Set.of());
        final User owner = new User("12345", "some@test.com", "test", false, Set.of(), Set.of());
        final Board board = buildBoard(1, EnumStateDto.CREATED, 5, owner, Set.of(), Set.of());
        final BoardCard boardCard = buildBoardCard(cardId, board, BoardCardsColumn.SUCCESS, owner, List.of());

        // when
        when(userRepository.findUserByEmail(email)).thenReturn(Optional.of(user));
        when(boardCardsRepository.findById(cardId)).thenReturn(Optional.of(boardCard));

        //then
        assertThrows(BadRequestException.class, () -> boardCardService.removeCard(cardId, email));
    }

    @Test
    void removeBoardCardShouldThrowNotFoundExceptionWhenBoardCardIsNotFound() {
        // given
        final Integer cardId = 1;
        final String email = "test22@test.com";
        final String uid = "1234";
        final User user = new User(uid, email, "john14", false, Set.of(), Set.of());

        //when
        when(userRepository.findUserByEmail(email)).thenReturn(Optional.of(user));
        when(boardCardsRepository.findById(cardId)).thenReturn(Optional.empty());

        //then
        assertThrows(NotFoundException.class, () -> boardCardService.removeCard(cardId, email));
    }

    @Test
    @DisplayName("addVote should throw Bad Request when user not exists")
    void addVoteShouldThrowBadRequestWhenUserIsNotFound() {
        // given
        final Integer cardId = 1;
        final String email = "test@example.com";
        final String expectedExceptionMessage = "User not found";

        //when
        when(userRepository.findUserByEmail(email)).thenReturn(Optional.empty());

        //then
        final BadRequestException exception =
                assertThrows(BadRequestException.class, () -> boardCardService.addVote(cardId, email));
        assertEquals(expectedExceptionMessage, exception.getMessage());
    }

    @Test
    @DisplayName("addVote should throw NotFound when board card not exists")
    void addVoteShouldThrowNotFoundWhenBoardCardIsNotFound() {
        // given
        final Integer cardId = 1;
        final String email = "test@example.com";
        final User user = new User("1234", email, "somename", false, Set.of(), Set.of());
        final String expectedExceptionMessage = "Card not found";

        //when
        when(userRepository.findUserByEmail(email)).thenReturn(Optional.of(user));
        when(boardCardsRepository.findById(cardId)).thenReturn(Optional.empty());

        //then
        final NotFoundException exception =
                assertThrows(NotFoundException.class, () -> boardCardService.addVote(cardId, email));
        assertEquals(expectedExceptionMessage, exception.getMessage());
    }

    @Test
    @DisplayName("addVote should throw BadRequest when board not exists")
    void addVoteShouldThrowBadRequestWhenBoardNotExist() {
        // given
        final Integer cardId = 1;
        final String email = "test@example.com";
        final User user = new User("1234", email, "somename", false, Set.of(), Set.of());
        final Board board = buildBoard(2, EnumStateDto.CREATED, 5, user, Set.of(), Set.of());
        final BoardCard card = buildBoardCard(cardId, board, BoardCardsColumn.SUCCESS, user, List.of());
        final String expectedExceptionMessage = "Board not exist";

        //when
        when(userRepository.findUserByEmail(email)).thenReturn(Optional.of(user));
        when(boardCardsRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(boardRepository.findById(card.getBoard().getId())).thenReturn(Optional.empty());

        //then
        final BadRequestException exception =
                assertThrows(BadRequestException.class, () -> boardCardService.addVote(cardId, email));
        assertEquals(expectedExceptionMessage, exception.getMessage());
    }

    @Test
    @DisplayName("addVote should throw BadRequest when user is not assigned to board")
    void addVoteShouldThrowBadRequestWhenUserIsNotAssignedToBoard() {
        // given
        final Integer cardId = 1;
        final String email = "test@example.com";
        final User creator = new User("123", "some@example.com", "somename", false, Set.of(), Set.of());
        final User user = new User("1234", email, "somename", false, Set.of(), Set.of());
        final Board board = buildBoard(2, EnumStateDto.CREATED, 5, creator, Set.of(), Set.of());
        final BoardCard card = buildBoardCard(cardId, board, BoardCardsColumn.SUCCESS, creator, List.of());
        final String expectedExceptionMessage = "User not assigned to board";

        //when
        when(userRepository.findUserByEmail(email)).thenReturn(Optional.of(user));
        when(boardCardsRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(boardRepository.findById(card.getBoard().getId())).thenReturn(Optional.of(board));

        //then
        final BadRequestException exception =
                assertThrows(BadRequestException.class, () -> boardCardService.addVote(cardId, email));
        assertEquals(expectedExceptionMessage, exception.getMessage());
    }

    @Test
    @DisplayName("addVote should throw BadRequest when board is in different state than voting")
    void addVoteShouldThrowBadRequestWhenBoardIsNotInStateVoting() {
        // given
        final Integer cardId = 1;
        final String email = "test@example.com";
        final User user = new User("1234", email, "somename", false, Set.of(), Set.of());
        final Board board = buildBoard(2, EnumStateDto.CREATED, 5, user, Set.of(user), new HashSet<>());
        final BoardCard card = buildBoardCard(cardId, board, BoardCardsColumn.SUCCESS, user, List.of());
        board.setBoardCards(Set.of(card));
        final String expectedExceptionMessage = "Wrong state of board";

        //when
        when(userRepository.findUserByEmail(email)).thenReturn(Optional.of(user));
        when(boardCardsRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(boardRepository.findById(card.getBoard().getId())).thenReturn(Optional.of(board));

        //then
        final BadRequestException exception =
                assertThrows(BadRequestException.class, () -> boardCardService.addVote(cardId, email));
        assertEquals(expectedExceptionMessage, exception.getMessage());
    }

    @Test
    @DisplayName("addVote should throw BadRequest when remaining votes are less than 0")
    void addVoteShouldThrowBadRequestWhenUsersRemainingVotesAreLessThan0() {
        // given
        final Integer cardId = 1;
        final String email = "test@example.com";
        final User user = new User("1234", email, "somename", false, Set.of(), Set.of());
        final Board board = buildBoard(2, EnumStateDto.VOTING, 5, user, Set.of(user), new HashSet<>());
        final BoardCard card = buildBoardCard(cardId, board, BoardCardsColumn.SUCCESS, user, List.of());
        board.setBoardCards(Set.of(card));
        final BoardCardVotesKey key = new BoardCardVotesKey(cardId, user.getUid());
        final BoardCardVotes boardCardVotes = new BoardCardVotes(key, card, user, 5);
        final String expectedExceptionMessage = "No more votes";

        //when
        when(userRepository.findUserByEmail(email)).thenReturn(Optional.of(user));
        when(boardCardsRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(boardRepository.findById(card.getBoard().getId())).thenReturn(Optional.of(board));
        when(boardCardsVotesRepository.getVotesByBoardAndUser(board, user)).thenReturn(
                List.of(boardCardVotes.getVotes()));

        //then
        final BadRequestException exception =
                assertThrows(BadRequestException.class, () -> boardCardService.addVote(cardId, email));
        assertEquals(expectedExceptionMessage, exception.getMessage());
    }

    @Test
    @DisplayName("addVote should update value of votes when BoardCardVotes already exists")
    void addVoteShouldUpdateVotesNumberWhenBoardCardVoteExists() {
        // given
        final Integer cardId = 1;
        final String email = "test@example.com";
        final User user = new User("1234", email, "somename", false, Set.of(), Set.of());
        final Board board = buildBoard(2, EnumStateDto.VOTING, 5, user, Set.of(user), new HashSet<>());
        final BoardCard card = buildBoardCard(cardId, board, BoardCardsColumn.SUCCESS, user, List.of());
        board.setBoardCards(Set.of(card));
        final BoardCardVotesKey key = new BoardCardVotesKey(cardId, user.getUid());
        final BoardCardVotes boardCardVotes = new BoardCardVotes(key, card, user, 4);
        final int votesToCheck = boardCardVotes.getVotes() + 1;

        //when
        when(userRepository.findUserByEmail(email)).thenReturn(Optional.of(user));
        when(boardCardsRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(boardRepository.findById(card.getBoard().getId())).thenReturn(Optional.of(board));
        when(boardCardsVotesRepository.getVotesByBoardAndUser(board, user)).thenReturn(
                List.of(boardCardVotes.getVotes()));
        when(boardCardsVotesRepository.findByCardAndVoter(card, user)).thenReturn(Optional.of(boardCardVotes));
        boardCardService.addVote(cardId, email);

        //then
        verify(boardCardsVotesRepository).save(any(BoardCardVotes.class));
        assertEquals(boardCardVotes.getVotes(), votesToCheck);
    }

    @Test
    @DisplayName("addVote should return number of remaining votes")
    void addVoteShouldReturnNumberOfRemainingVotes() {
        // given
        final Integer cardId = 1;
        final String email = "test@example.com";
        final User user = new User("1234", email, "somename", false, Set.of(), Set.of());
        final Board board = buildBoard(2, EnumStateDto.VOTING, 10, user, Set.of(user), new HashSet<>());
        final BoardCard card = buildBoardCard(cardId, board, BoardCardsColumn.FAILURES, user, List.of());
        board.setBoardCards(Set.of(card));
        final BoardCardVotesKey key = new BoardCardVotesKey(cardId, user.getUid());
        final BoardCardVotes boardCardVotes = new BoardCardVotes(key, card, user, 4);
        final int remainingVotes = board.getMaximumNumberOfVotes() - boardCardVotes.getVotes() - 1;

        //when
        when(userRepository.findUserByEmail(email)).thenReturn(Optional.of(user));
        when(boardCardsRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(boardRepository.findById(card.getBoard().getId())).thenReturn(Optional.of(board));
        when(boardCardsVotesRepository.getVotesByBoardAndUser(board, user)).thenReturn(
                List.of(boardCardVotes.getVotes()));
        when(boardCardsVotesRepository.findByCardAndVoter(card, user)).thenReturn(Optional.empty());
        final Map<String, Integer> remainingVotesMap = boardCardService.addVote(cardId, email);

        //then
        verify(boardCardsVotesRepository).save(any(BoardCardVotes.class));
        assertTrue(remainingVotesMap.containsKey("remainingVotes"));
        assertTrue(remainingVotesMap.containsValue(remainingVotes));
    }

    @Test
    @DisplayName("Remove vote should throw bad request when user is not assigned to board")
    void removeVoteShouldThrowBadRequestWhenUserIsNotAssignedToBoard() {
        // given
        final Integer cardId = 1;
        final String email = "test@example.com";
        final String fakeEmail = "fakeTest@example.com";
        final User user = new User("1234", email, "somename", false, Set.of(), Set.of());
        final User fakeUser = new User("5678", fakeEmail, "somename", false, Set.of(), Set.of());
        final Board board = buildBoard(2, EnumStateDto.VOTING, 10, user, Set.of(user), new HashSet<>());
        final BoardCard card = buildBoardCard(cardId, board, BoardCardsColumn.FAILURES, user, List.of());
        board.setBoardCards(Set.of(card));

        //when
        when(userRepository.findUserByEmail(fakeEmail)).thenReturn(Optional.of(fakeUser));
        when(boardCardsRepository.findById(cardId)).thenReturn(Optional.of(card));

        //then
        final BadRequestException exception = assertThrows(
                BadRequestException.class, () -> boardCardService.removeVote(cardId, fakeEmail));
        assertEquals("User not assigned to board", exception.getMessage());
    }

    @Test
    @DisplayName("Remove vote should throw not found when card is not found")
    void removeVoteShouldNotFoundWhenCardIsNotFound() {
        // given
        final Integer cardId = 1;
        final String email = "test@example.com";
        final User user = new User("1234", email, "somename", false, Set.of(), Set.of());
        final Board board = buildBoard(2, EnumStateDto.VOTING, 10, user, Set.of(user), new HashSet<>());
        final BoardCard card = buildBoardCard(cardId, board, BoardCardsColumn.FAILURES, user, List.of());
        board.setBoardCards(Set.of(card));

        //when
        when(userRepository.findUserByEmail(email)).thenReturn(Optional.of(user));
        when(boardCardsRepository.findById(cardId)).thenReturn(Optional.empty());

        //then
        final NotFoundException exception = assertThrows(
                NotFoundException.class, () -> boardCardService.removeVote(cardId, email));
        assertEquals("Card not found", exception.getMessage());
    }

    @Test
    @DisplayName("Remove vote should throw bad request when board is not in state voting")
    void removeVoteShouldThrowBadRequestWhenBoardIsNotInStateVoting() {
        // given
        final Integer cardId = 1;
        final String email = "test@example.com";
        final User user = new User("1234", email, "somename", false, Set.of(), Set.of());
        final Board board = buildBoard(2, EnumStateDto.CREATED, 10, user, Set.of(user), new HashSet<>());
        final BoardCard card = buildBoardCard(cardId, board, BoardCardsColumn.FAILURES, user, List.of());
        board.setBoardCards(Set.of(card));

        //when
        when(userRepository.findUserByEmail(email)).thenReturn(Optional.of(user));
        when(boardCardsRepository.findById(cardId)).thenReturn(Optional.of(card));

        //then
        final BadRequestException exception = assertThrows(
                BadRequestException.class, () -> boardCardService.removeVote(cardId, email));
        assertEquals("Wrong state of board", exception.getMessage());
    }

    @Test
    @DisplayName("Remove vote should throw bad request when user has no more votes to remove")
    void removeVoteShouldThrowBadRequestWhenUserHasNoMoreVotesToRemove() {
        // given
        final Integer cardId = 1;
        final String email = "test@example.com";
        final User user = new User("1234", email, "somename", false, Set.of(), Set.of());
        final Board board = buildBoard(2, EnumStateDto.VOTING, 2, user, Set.of(user), new HashSet<>());
        final BoardCard card = buildBoardCard(cardId, board, BoardCardsColumn.FAILURES, user, List.of());
        board.setBoardCards(Set.of(card));

        //when
        when(userRepository.findUserByEmail(email)).thenReturn(Optional.of(user));
        when(boardCardsRepository.findById(cardId)).thenReturn(Optional.of(card));

        //then
        final BadRequestException exception = assertThrows(
                BadRequestException.class, () -> boardCardService.removeVote(cardId, email));
        assertEquals("User has no votes to remove", exception.getMessage());
    }

    @Test
    @DisplayName("Remove vote should return number of remaining votes")
    void removeVoteShouldReturnNumberOfRemainingVotes() {
        // given
        final Integer cardId = 1;
        final String email = "test@example.com";
        final User user = new User("1234", email, "somename", false, Set.of(), Set.of());
        final Board board = buildBoard(2, EnumStateDto.VOTING, 10, user, Set.of(user), new HashSet<>());
        final BoardCard card = buildBoardCard(cardId, board, BoardCardsColumn.FAILURES, user, List.of());
        board.setBoardCards(Set.of(card));
        final BoardCardVotesKey key = new BoardCardVotesKey(cardId, user.getUid());
        final BoardCardVotes boardCardVotes = new BoardCardVotes(key, card, user, 4);

        //when
        when(userRepository.findUserByEmail(email)).thenReturn(Optional.of(user));
        when(boardCardsRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(boardCardsVotesRepository.findByCardAndVoter(card, user)).thenReturn(Optional.of(boardCardVotes));
        final Map<String, Integer> removeVote = boardCardService.removeVote(cardId, email);

        //then
        assertEquals(removeVote.get("remainingVotes"),7);
    }

    @Test
    @DisplayName("Remove vote should delete BoardCardVotes entity after removing last vote")
    void removeVoteShouldDeleteVoteEntityWhenThereIsNoVotes() {
        // given
        final Integer cardId = 1;
        final String email = "test@example.com";
        final User user = new User("1234", email, "somename", false, Set.of(), Set.of());
        final Board board = buildBoard(2, EnumStateDto.VOTING, 4, user, Set.of(user), new HashSet<>());
        final BoardCard card = buildBoardCard(cardId, board, BoardCardsColumn.FAILURES, user, List.of());
        board.setBoardCards(Set.of(card));
        final BoardCardVotesKey key = new BoardCardVotesKey(cardId, user.getUid());
        final BoardCardVotes boardCardVotes = new BoardCardVotes(key, card, user, 1);

        //when
        when(userRepository.findUserByEmail(email)).thenReturn(Optional.of(user));
        when(boardCardsRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(boardCardsVotesRepository.findByCardAndVoter(card, user)).thenReturn(Optional.of(boardCardVotes));

        //then
        assertEquals(Map.of("remainingVotes", 4), boardCardService.removeVote(cardId, email));
        verify(boardCardsVotesRepository).delete(any());
    }

    @Test
    @DisplayName("When addCardAction() is called and the user is not found -> Not found Exception is thrown.")
    void addCardActionThrowsUserNotFoundIfUserIsNotProvided() {
        //given
        when(userRepository.findUserByEmail(any())).thenReturn(Optional.empty());

        //when & then
        final NotFoundException exception = assertThrows(
                NotFoundException.class, () -> boardCardService.addCardAction(null, null, null));
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    @DisplayName("When addCardAction() is called and the card is not found -> Not found Exception is thrown")
    void addCardActionThrowsCardNotFoundIfCardDoesNotExist() {
        //given
        final User user = new User("1234", "test@example.com", "somename", false, Set.of(), Set.of());
        when(userRepository.findUserByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(boardCardsRepository.findById(any())).thenReturn(Optional.empty());

        //when & then
        final NotFoundException exception = assertThrows(
                NotFoundException.class, () -> boardCardService.addCardAction(1, user.getEmail(), null));
        assertEquals("Card not found", exception.getMessage());
    }

    @Test
    @DisplayName("When addCardAction() is called and state of the board is actions -> board card action is created.")
    void addCardActionCreatesCardActionWhenStateIsActions() {
        //given
        final User user = new User("1234", "test@example.com", "somename", false, Set.of(), Set.of());
        final Board board = buildBoard(1, EnumStateDto.ACTIONS, 4, user, Set.of(user), new HashSet<>());
        final BoardCard card = buildBoardCard(1, board, BoardCardsColumn.FAILURES, user, List.of());
        final BoardCardActionDto expectedResponse = BoardCardActionDto.builder()
                .cardId(1)
                .text("test")
                .build();
        when(userRepository.findUserByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(boardRepository.findById(card.getBoard().getId())).thenReturn(Optional.of(board));
        when(boardCardsRepository.findById(card.getId())).thenReturn(Optional.of(card));

        //when
        final BoardCardActionDto responseDto = boardCardService
                .addCardAction(1, "test@example.com", new BoardCardActionRequestDto("test"));

        //then
        verify(boardCardsActionsRepository).save(any(BoardCardAction.class));
        assertEquals(expectedResponse, responseDto);
    }

    @Test
    @DisplayName("When addCardAction() is called and state of the board is not actions -> Bad Request Exception is thrown.")
    void addCardActionThrowsBadRequestExceptionWhenStateIsNotActions() {
        //given
        final User user = new User("1234", "test@example.com", "somename", false, Set.of(), Set.of());
        final Board board = buildBoard(1, EnumStateDto.VOTING, 4, user, Set.of(user), new HashSet<>());
        final BoardCard card = buildBoardCard(1, board, BoardCardsColumn.FAILURES, user, List.of());
        when(userRepository.findUserByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(boardRepository.findById(card.getBoard().getId())).thenReturn(Optional.of(board));
        when(boardCardsRepository.findById(card.getId())).thenReturn(Optional.of(card));

        //when & then
        final BadRequestException exception = assertThrows(
                BadRequestException.class, () -> boardCardService.addCardAction
                        (1, user.getEmail(), new BoardCardActionRequestDto("test")));
        assertEquals("State is not actions", exception.getMessage());
    }

    @Test
    @DisplayName("When Board's state is ACTIONS and owner of the board tries to remove action, " +
            "then it should succeed")
    void removeActionShouldPassWhenOwnerTriesToRemoveExistingActionWhenBoardStateIsActions() {
        // given
        final Integer actionId = 1;
        final String email = "some@test.com";
        final User user = new User("1234", email, "some", false, Set.of(), Set.of());
        final Board board = buildBoard(1, EnumStateDto.ACTIONS, 0, user, Set.of(), Set.of());
        final BoardCard boardCard = buildBoardCard(1, board, BoardCardsColumn.SUCCESS, user, List.of());
        final BoardCardAction action = new BoardCardAction(actionId, boardCard, "sometext");

        // when
        when(userRepository.findUserByEmail(email)).thenReturn(Optional.of(user));
        when(boardCardsActionsRepository.findById(actionId)).thenReturn(Optional.of(action));

        // then
        boardCardService.removeAction(actionId, email);

        final ArgumentCaptor<Integer> actionIdCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(boardCardsActionsRepository).deleteById(actionIdCaptor.capture());

        assertEquals(actionId, actionIdCaptor.getValue());
    }

    @Test
    void removeActionShouldFailWhenBoardStateIsNotActions() {
        // given
        final Integer actionId = 1;
        final String email = "some@test.com";
        final User user = new User("1234", email, "some", false, Set.of(), Set.of());
        final Board board = buildBoard(1, EnumStateDto.DONE, 0, user, Set.of(), Set.of());
        final BoardCard boardCard = buildBoardCard(1, board, BoardCardsColumn.SUCCESS, user, List.of());
        final BoardCardAction action = new BoardCardAction(actionId, boardCard, "sometext");
        final String expectedMessage = "Wrong board's state";

        // when
        when(userRepository.findUserByEmail(email)).thenReturn(Optional.of(user));
        when(boardCardsActionsRepository.findById(actionId)).thenReturn(Optional.of(action));

        // then
        final BadRequestException e = assertThrows(BadRequestException.class,
                () -> boardCardService.removeAction(actionId, email));
        assertEquals(expectedMessage, e.getMessage());
    }

    @Test
    void removeActionShouldFailWhenUserIsNotOwner() {
        // given
        final Integer actionId = 1;
        final String email = "some@test.com";
        final User owner = new User("1234", "owner@test.com", "owner", false, Set.of(), Set.of());
        final User user = new User("12345", email, "some", false, Set.of(), Set.of());
        final Board board = buildBoard(1, EnumStateDto.ACTIONS, 0, owner, Set.of(), Set.of());
        final BoardCard boardCard = buildBoardCard(1, board, BoardCardsColumn.SUCCESS, owner, List.of());
        final BoardCardAction action = new BoardCardAction(actionId, boardCard, "sometext");
        final String expectedMessage = "You are not owner";

        // when
        when(userRepository.findUserByEmail(email)).thenReturn(Optional.of(user));
        when(boardCardsActionsRepository.findById(actionId)).thenReturn(Optional.of(action));

        // then
        final BadRequestException e = assertThrows(BadRequestException.class,
                () -> boardCardService.removeAction(actionId, email));
        assertEquals(expectedMessage, e.getMessage());
    }

    @Test
    void removeActionShouldFailWhenActionDoesNotExist() {
        // given
        final Integer actionId = 1;

        // when
        when(boardCardsActionsRepository.findById(actionId)).thenReturn(Optional.empty());

        // then
        assertThrows(NotFoundException.class, () -> boardCardService.removeAction(actionId, any()));
    }

    private Board buildBoard(final int id, final EnumStateDto state, final int numberOfVotes, final User user,
                             final Set<User> users, final Set<BoardCard> cards) {
        return Board.builder()
                .id(id)
                .name("board name")
                .state(state)
                .maximumNumberOfVotes(numberOfVotes)
                .creator(user)
                .users(users)
                .boardCards(cards)
                .build();
    }

    private BoardCard buildBoardCard(final int id, final Board board, final BoardCardsColumn column, final User user,
                                     final List<BoardCardAction> actions) {
        return BoardCard.builder()
                .id(id)
                .board(board)
                .text("card text")
                .column(column)
                .creator(user)
                .boardCardActions(actions)
                .build();
    }
}