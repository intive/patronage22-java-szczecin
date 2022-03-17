package com.intive.patronage22.szczecin.retroboard.service;

import com.intive.patronage22.szczecin.retroboard.dto.BoardCardDto;
import com.intive.patronage22.szczecin.retroboard.dto.BoardCardsColumn;
import com.intive.patronage22.szczecin.retroboard.dto.EnumStateDto;
import com.intive.patronage22.szczecin.retroboard.exception.BadRequestException;
import com.intive.patronage22.szczecin.retroboard.exception.NotFoundException;
import com.intive.patronage22.szczecin.retroboard.model.Board;
import com.intive.patronage22.szczecin.retroboard.model.BoardCard;
import com.intive.patronage22.szczecin.retroboard.model.BoardCardAction;
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

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
        final Board board = buildBoard(boardId, EnumStateDto.CREATED, user, Set.of(), Set.of());

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
        final Board board = buildBoard(boardId, EnumStateDto.VOTING, user, Set.of(), Set.of());

        //when
        when(userRepository.findUserByEmail(email)).thenReturn(Optional.of(user));
        when(boardRepository.existsById(boardId)).thenReturn(true);
        when(boardRepository.findBoardByIdAndCreatorOrAssignedUser(boardId, user)).thenReturn(Optional.of(board));

        //then
        assertThrows(BadRequestException.class, () -> boardCardService.createBoardCard(requestDto, boardId, email));
    }

    @Test
    @DisplayName("Vote should throw Bad Request when user not exists")
    void voteShouldThrowBadRequestWhenUserIsNotFound() {
        // given
        final Integer cardId = 1;
        final String email = "test22@test.com";

        //when
        when(userRepository.findUserByEmail(email)).thenReturn(Optional.empty());

        //then
        assertThrows(BadRequestException.class, () -> boardCardService.vote(cardId, email));
    }

    @Test
    @DisplayName("Vote should throw NotFound when board card not exists")
    void voteShouldThrowNotFoundWhenBoardCardIsNotFound() {
        // given
        final Integer cardId = 1;
        final String email = "test@example.com";
        final User user = new User("1234", email, "somename", Set.of(), Set.of());

        //when
        when(userRepository.findUserByEmail(email)).thenReturn(Optional.of(user));
        when(boardCardsRepository.findById(cardId)).thenReturn(Optional.empty());

        //then
        assertThrows(NotFoundException.class, () -> boardCardService.vote(cardId, email));
    }

    @Test
    @DisplayName("Vote should throw BadRequest when board not exists")
    void voteShouldThrowBadRequestWhenBoardNotExist() {
        // given
        final Integer cardId = 1;
        final String email = "test@example.com";
        final User user = new User("1234", email, "somename", Set.of(), Set.of());
        final Board board = buildBoard(2, EnumStateDto.CREATED, user, Set.of(), Set.of());
        final BoardCard card = buildBoardCard(cardId, board , BoardCardsColumn.SUCCESS, user, List.of());

        //when
        when(userRepository.findUserByEmail(email)).thenReturn(Optional.of(user));
        when(boardCardsRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(boardRepository.findById(card.getBoard().getId())).thenReturn(Optional.empty());

        //then
        assertThrows(BadRequestException.class, () -> boardCardService.vote(cardId, email));
    }

    private Board buildBoard(final int id, final EnumStateDto state, final User user, final Set<User> users,
                             final Set<BoardCard> cards) {
        return Board.builder()
                .id(id)
                .name("board name")
                .state(state)
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

/*

  @Transactional
    public Map<String, Integer> vote(final Integer cardId, final String email) {


        if (!board.getUsers().contains(user) && !board.getCreator().equals(user)) {
            throw new BadRequestException("User not assigned to board");
        }

        if (!board.getState().equals(EnumStateDto.VOTING)) {
            throw new BadRequestException("Wrong state of board");
        }

        final int addedUserVotes =
                boardCardsVotesRepository.getCountsByBoardAndUser(board, user)
                        .stream()
                        .reduce(0, Integer::sum);

        final int remainingUserVotes = board.getMaximumNumberOfVotes() - addedUserVotes;
        if (remainingUserVotes == 0) {
            throw new BadRequestException("No more votes");
        }

        boardCardsVotesRepository.findByCardAndVoter(card, user).ifPresentOrElse(vote -> {
            vote.setVotes(vote.getVotes() + 1);
            boardCardsVotesRepository.save(vote);
        }, () -> {
            final BoardCardVotesKey key = new BoardCardVotesKey(card.getId(), user.getUid());
            boardCardsVotesRepository.save(new BoardCardVotes(key, card, user, 1));
        });

        return Map.of("remainingVotes", remainingUserVotes - 1);
    }
 */