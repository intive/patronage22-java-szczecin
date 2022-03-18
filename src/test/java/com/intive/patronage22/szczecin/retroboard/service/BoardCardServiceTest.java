package com.intive.patronage22.szczecin.retroboard.service;

import com.intive.patronage22.szczecin.retroboard.dto.BoardCardDto;
import com.intive.patronage22.szczecin.retroboard.dto.BoardCardsColumn;
import com.intive.patronage22.szczecin.retroboard.dto.EnumStateDto;
import com.intive.patronage22.szczecin.retroboard.exception.BadRequestException;
import com.intive.patronage22.szczecin.retroboard.exception.NotFoundException;
import com.intive.patronage22.szczecin.retroboard.model.Board;
import com.intive.patronage22.szczecin.retroboard.model.BoardCard;
import com.intive.patronage22.szczecin.retroboard.model.BoardCardAction;
import com.intive.patronage22.szczecin.retroboard.model.BoardCardVotes;
import com.intive.patronage22.szczecin.retroboard.model.BoardCardVotesKey;
import com.intive.patronage22.szczecin.retroboard.model.User;
import com.intive.patronage22.szczecin.retroboard.repository.BoardCardsRepository;
import com.intive.patronage22.szczecin.retroboard.repository.BoardCardsVotesRepository;
import com.intive.patronage22.szczecin.retroboard.repository.BoardRepository;
import com.intive.patronage22.szczecin.retroboard.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
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
        final User user = new User(uid, email, "john14", Set.of(), Set.of());
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
        final User user = new User(uid, email, "john14", Set.of(), Set.of());

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
        final User user = new User(uid, email, "john14", Set.of(), Set.of());

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
        final User user = new User(uid, email, "john14", Set.of(), Set.of());
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
        final User user = new User("1234", email, "john14", Set.of(), Set.of());
        final User boardOwner = new User("12345", "some@test.com", "test", Set.of(), Set.of());
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
        final User boardOwner = new User("1234", email, "john14", Set.of(), Set.of());
        final User user = new User("12345", "some@test.com", "test", Set.of(), Set.of());
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
        final User user = new User("1234", email, "john14", Set.of(), Set.of());
        final User boardOwner = new User("12345", "some@test.com", "test", Set.of(), Set.of());
        final Board board = buildBoard(1, EnumStateDto.CREATED, 5, boardOwner, Set.of(), Set.of());
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
        final User user = new User("1234", email, "john14", Set.of(), Set.of());
        final User owner = new User("12345", "some@test.com", "test", Set.of(), Set.of());
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
        final User user = new User(uid, email, "john14", Set.of(), Set.of());

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
        final User user = new User("1234", email, "somename", Set.of(), Set.of());
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
        final User user = new User("1234", email, "somename", Set.of(), Set.of());
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
        final User creator = new User("123", "some@example.com", "somename", Set.of(), Set.of());
        final User user = new User("1234", email, "somename", Set.of(), Set.of());
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
        final User user = new User("1234", email, "somename", Set.of(), Set.of());
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
        final User user = new User("1234", email, "somename", Set.of(), Set.of());
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
        final User user = new User("1234", email, "somename", Set.of(), Set.of());
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
        final User user = new User("1234", email, "somename", Set.of(), Set.of());
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
                                     List<BoardCardAction> actions) {
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