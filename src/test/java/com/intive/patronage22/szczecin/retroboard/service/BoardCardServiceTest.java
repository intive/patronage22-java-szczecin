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
import com.intive.patronage22.szczecin.retroboard.repository.BoardRepository;
import com.intive.patronage22.szczecin.retroboard.repository.UserRepository;
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
        final Board board = Board.builder()
                .id(boardId)
                .name("board name")
                .state(EnumStateDto.CREATED)
                .creator(user)
                .users(Set.of())
                .boardCards(Set.of())
                .build();

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
        final Integer orderNumber = 0;

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
        final User boardOwner = new User("12456", "boardowner@email.com", "owner", Set.of(), Set.of());
        final Board board = Board.builder()
                .id(boardId)
                .name("board name")
                .state(EnumStateDto.CREATED)
                .creator(boardOwner)
                .users(Set.of())
                .boardCards(Set.of())
                .build();

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
        final Board board = Board.builder()
                .id(boardId)
                .name("board name")
                .state(EnumStateDto.VOTING)
                .creator(user)
                .users(Set.of())
                .boardCards(Set.of())
                .build();

        //when
        when(userRepository.findUserByEmail(email)).thenReturn(Optional.of(user));
        when(boardRepository.existsById(boardId)).thenReturn(true);
        when(boardRepository.findBoardByIdAndCreatorOrAssignedUser(boardId, user)).thenReturn(Optional.of(board));

        //then
        assertThrows(BadRequestException.class, () -> boardCardService
                .createBoardCard(requestDto, boardId, email));
    }

    @Test
    void removeBoardCardShouldPassWhenUserOwnsBoardCard() {
        // given
        final Integer cardId = 1;
        final String email = "test22@test.com";
        final User user = new User("1234", email, "john14", Set.of(), Set.of());
        final User boardOwner = new User("12345", "some@test.com", "test", Set.of(), Set.of());
        final Board board = buildBoard(boardOwner, EnumStateDto.CREATED, 1, Set.of());
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
        final Board board = buildBoard(boardOwner, EnumStateDto.CREATED, 1, Set.of());
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
        final Board board = buildBoard(boardOwner, EnumStateDto.VOTING, 1, Set.of());
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
        final Board board = buildBoard(owner, EnumStateDto.CREATED, 1, Set.of());
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

    private Board buildBoard(final User user, final EnumStateDto state, final int id, final Set<User> users) {
        return Board.builder()
                .id(id)
                .name("My first board.")
                .state(state)
                .creator(user)
                .users(users)
                .build();
    }

    private BoardCard buildBoardCard(final int id, final Board board, final BoardCardsColumn column,
                                     final User creator, final List<BoardCardAction> boardCardActions) {
        return BoardCard.builder()
                .id(id)
                .board(board)
                .text("some text")
                .column(column)
                .creator(creator)
                .boardCardActions(boardCardActions)
                .build();
    }
}