package com.intive.patronage22.szczecin.retroboard.service;

import com.intive.patronage22.szczecin.retroboard.dto.*;
import com.intive.patronage22.szczecin.retroboard.exception.BadRequestException;
import com.intive.patronage22.szczecin.retroboard.exception.NotFoundException;
import com.intive.patronage22.szczecin.retroboard.model.Board;
import com.intive.patronage22.szczecin.retroboard.model.BoardCard;
import com.intive.patronage22.szczecin.retroboard.model.BoardCardAction;
import com.intive.patronage22.szczecin.retroboard.model.User;
import com.intive.patronage22.szczecin.retroboard.repository.BoardCardsRepository;
import com.intive.patronage22.szczecin.retroboard.repository.BoardRepository;
import com.intive.patronage22.szczecin.retroboard.repository.UserRepository;
import com.intive.patronage22.szczecin.retroboard.validation.BoardValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = BoardService.class)
class BoardServiceTest {

    @Autowired
    private BoardService boardService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private BoardRepository boardRepository;

    @MockBean
    private BoardValidator boardValidator;

    @MockBean
    BoardCardsRepository boardCardsRepository;

    @MockBean
    UserService userService;

    @Test
    void getUserBoardsShouldReturnOk() {
        //given
        final String uid = "1234";
        final String email = "John@test.pl";
        final User user = new User(uid, email, "john14", Set.of());
        final Board board = Board.builder()
                .id(1)
                .name("board name")
                .state(EnumStateDto.CREATED)
                .creator(user)
                .users(Set.of())
                .boardCards(Set.of())
                .build();
        user.setUserBoards(Set.of(board));

        //when
        when(userRepository.findUserByEmail(email)).thenReturn(Optional.of(user));
        final List<BoardDto> boards = boardService.getUserBoards(email);

        //then
        assertEquals(boards.get(0).getId(), board.getId());
        assertEquals(boards.get(0).getName(), board.getName());
        assertEquals(boards.get(0).getState(), board.getState());
    }

    @Test
    void getUserBoardsShouldThrowBadRequestWhenEmailIsBlank() {
        //given
        final String email = "";

        //when

        //then
        assertThrows(BadRequestException.class, () -> boardService.getUserBoards(email));
    }

    @Test
    void getUserBoardsShouldThrowBadRequestWhenEmailIsNull() {
        //given
        final String email = null;

        //when

        //then
        assertThrows(BadRequestException.class, () -> boardService.getUserBoards(email));
    }

    @Test
    void getUserBoardsShouldThrowBadRequestWhenUserDoesNotExist() {
        //given
        final String email = "some@test.com";

        //when
        when(userRepository.findUserByEmail(email)).thenThrow(BadRequestException.class);

        //then
        assertThrows(BadRequestException.class, () -> boardService.getUserBoards(email));
    }

    @Test
    void createBoardShouldReturnBoardDtoWhenUserExistsAndBoardNameIsValid() {
        // given
        final String uid = "uid101";
        final String email = "Josef@test.pl";
        final String boardName = "My first board.";

        final User user = new User(uid, email, "josef14", Set.of());

        final Board board = Board.builder()
                .id(10)
                .name(boardName)
                .state(EnumStateDto.CREATED)
                .creator(user)
                .users(Set.of())
                .build();

        // when
        when(userRepository.findUserByEmail(email)).thenReturn(Optional.of(user));
        when(boardRepository.save(any(Board.class))).thenReturn(board);
        final BoardDto boardDtoResult = boardService.createBoard(boardName, email);

        // then
        assertEquals(BoardDto.fromModel(board), boardDtoResult);
        verify(userRepository).findUserByEmail(email);
        verify(boardRepository).save(any(Board.class));
    }

    @Test
    void createBoardShouldReturnBadRequestWhenUserDoesNotExist() {
        // given
        final String email = "some@test.com";
        final String boardName = "My first board.";

        // when
        when(userRepository.findUserByEmail(email)).thenReturn(Optional.empty());

        // then
        assertThrows(BadRequestException.class, () -> boardService.createBoard(boardName, email));
    }

    @Test
    @DisplayName("getBoardDataById should throw 400 when user does not exist")
    void getBoardDataByIdShouldThrowBadRequestWhenUserDoesNotExist() {
        //given
        final int boardId = 1;
        final String email = "testemail@example.com";

        //when
        when(userRepository.findUserByEmail(email)).thenReturn(Optional.empty());

        //then
        assertThrows(BadRequestException.class, () -> boardService.getBoardDataById(boardId, email));
    }

    @Test
    @DisplayName("getBoardDataById should throw 404 when board does not exist")
    void getBoardDataByIdShouldThrowNotFoundWhenBoardDoesNotExist() {
        //given
        final int boardId = 1;
        final String email = "testemail@example.com";
        final String displayName = "test";
        final User user = new User("123", email, displayName, Set.of());

        //when
        when(userRepository.findUserByEmail(email)).thenReturn(Optional.of(user));
        when(boardRepository.findById(boardId)).thenReturn(Optional.empty());

        //then
        assertThrows(NotFoundException.class, () -> boardService.getBoardDataById(boardId, email));
    }

    @Test
    @DisplayName("getBoardDataById should throw 400 when user has no permission to view board")
    void getBoardDataByIdShouldThrowBadRequestWhenUserDoesntHavePermissions() {
        //given
        final int boardId = 1;
        final String email = "testemail@example.com";
        final String displayName = "test12";
        final User user = new User("123", email, displayName, Set.of());
        final User assignUser = new User("1234", "assignUser@test.pl", "test1", Set.of());
        final Board board = Board.builder()
                .id(1)
                .name("board name")
                .state(EnumStateDto.CREATED)
                .creator(user)
                .users(Set.of(assignUser))
                .boardCards(Set.of()).build();

        //when
        when(userRepository.findUserByEmail(email)).thenReturn(Optional.of(user));
        when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
        when(boardRepository.findBoardByIdAndCreatorOrAssignedUser(boardId, user)).thenReturn(Optional.empty());

        //then
        assertThrows(BadRequestException.class, () -> boardService.getBoardDataById(boardId, email));
    }

    @Test
    @DisplayName("getBoardDataById should return 200")
    void getBoardDataByIdShouldReturnOk() {
        //given
        final int boardId = 1;
        final String email = "testemail@example.com";
        final String displayName = "testDisplayName";
        final User user = new User("123", email, displayName, Set.of());
        final User assignUser = new User("1234", "assignUser", "test1", Set.of());
        final Board board = Board.builder()
                .id(boardId)
                .name("board name")
                .state(EnumStateDto.CREATED)
                .creator(user)
                .users(Set.of(assignUser))
                .boardCards(Set.of()).build();
        final BoardCard boardCard = new BoardCard
                (2, board, "test card name", BoardCardsColumn.SUCCESS, user, List.of());
        final BoardCardAction boardCardAction = new BoardCardAction(4, boardCard, "test action");
        board.setBoardCards(Set.of(boardCard));
        boardCard.setBoardCardActions(List.of(boardCardAction));
        final List<BoardCard> boardCards = List.of(boardCard);

        //when
        when(userRepository.findUserByEmail(email)).thenReturn(Optional.of(user));
        when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
        when(boardRepository.findBoardByIdAndCreatorOrAssignedUser(boardId, user)).thenReturn(Optional.of(board));
        when(boardCardsRepository.findAllByBoardId(boardId)).thenReturn(boardCards);

        final BoardDataDto boardDataDto = boardService.getBoardDataById(boardId, email);

        //then
        assertEquals(boardDataDto.getBoard().getId(), board.getId());
        assertEquals(boardDataDto.getBoard().getName(), board.getName());
        assertEquals(boardDataDto.getBoard().getState(), board.getState());
        assertEquals(boardDataDto.getBoardCards().get(0).getId(), boardCards.get(0).getId());
        assertEquals(boardDataDto.getBoardCards().get(0).getCardText(), boardCards.get(0).getText());
        assertEquals(boardDataDto.getBoardCards().get(0).getColumnName(), boardCards.get(0).getColumn());
        assertEquals(boardDataDto.getBoardCards().get(0).getBoardCardCreator(),
                boardCards.get(0).getCreator().getEmail());
        assertEquals(boardDataDto.getBoardCards().get(0).getActionTexts(),
                List.of(boardCards.get(0).getBoardCardActions().get(0).getText()));
    }

    @Test
    void deleteBoardShouldReturnNotFoundWhenBoardNotExist() {

        //given
        final String email = "some@test.com";
        final int boardId = 101;

        //when
        when(boardRepository.findById(boardId)).thenReturn(Optional.empty());

        //then
        assertThrows(NotFoundException.class,
                () -> boardService.delete(boardId, email));
    }

    @Test
    void deleteBoardShouldReturnBadRequestWhenBoardFoundAndUserIsNotOwner() {

        final String uidOwner = "123";
        final String uid = "1234";
        final String emailOwner = "owner@test.pl";
        final String email = "username@test.pl";
        final int boardId = 1;
        final BoardCard boardCard = new BoardCard();
        final User userOwner = new User(uidOwner, emailOwner, "displayName", Set.of());
        final User user = new User(uid, email, "displayName", Set.of());
        final Board board = new Board(boardId, "board", 0,
                EnumStateDto.CREATED, userOwner, Set.of(userOwner), Set.of(boardCard));

        //when
        when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
        when(userRepository.findUserByEmail(email)).thenReturn(Optional.of(board.getCreator()));

        //then
        assertThrows(BadRequestException.class,
                () -> boardService.delete(boardId, email));
    }

    @Test
    void deleteBoardShouldReturnBadRequestWhenBoardFoundButUserIsNotOwner() {

        final String uidOwner = "123";
        final String uid = "1234";
        final String emailOwner = "owner@test.pl";
        final String email = "username@test.pl";
        final int boardId = 1;
        final BoardCard boardCard = new BoardCard();
        final User userOwner = new User(uidOwner, emailOwner, "displayName", Set.of());
        final User user = new User(uid, email, "displayName", Set.of());
        final Board board = new Board
                (boardId, "board", 0,
                        EnumStateDto.CREATED, userOwner, Set.of(userOwner), Set.of(boardCard));

        //when
        when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));

        //then
        assertEquals(emailOwner, board.getCreator().getEmail());
        assertThrows(BadRequestException.class,
                () -> boardService.delete(boardId, email));
    }

    @Test
    void patchBoardShouldReturnNotFoundWhenBoardDoesNotExist() {
        // given
        final var email = "username@test.pl";
        final var id = 500;
        final var boardPatchDto = new BoardPatchDto("testboard", 1500);
        when(boardRepository.findById(id)).thenReturn(Optional.empty());

        // when & then
        assertThrows(NotFoundException.class,
                () -> boardService.patchBoard(id, boardPatchDto, email));
    }

    @Test
    void patchBoardShouldReturnUserIsNotAnOwner() {
        // given
        final var uid = "uid101";
        final var email = "username@test.pl";
        final var user = new User(uid, email, "displayName", Set.of());
        final var id = 10;
        final var board = buildBoard(user);
        when(boardRepository.findById(id)).thenReturn(Optional.of(board));
        final var boardPatchDto = new BoardPatchDto("testboard", 1500);


        // when & then
        assertThrows(BadRequestException.class,
                () -> boardService.patchBoard(id, boardPatchDto, "some@test.pl"));
    }

    @Test
    void patchBoardShouldUpdateNameAndNumberOfVotesFields() {
        // given
        final var uid = "uid101";
        final var email = "username@test.pl";
        final var boardName = "My first board.";
        final var userOwner = new User(uid, email, "displayName", Set.of());
        final var board = buildBoard(userOwner);
        final var id = 10;
        final var boardPatchDto = new BoardPatchDto(boardName, 1500);

        // when
        when(boardRepository.save(any(Board.class))).thenReturn(board);
        when(boardRepository.findById(id)).thenReturn(Optional.of(board));

        // then
        final var boardDtoResult = boardService.patchBoard(id, boardPatchDto, email);
        
        assertEquals(BoardDto.fromModel(board), boardDtoResult);
        verify(boardRepository).findById(id);
        verify(boardRepository).save(any(Board.class));
    }

    @Test
    void patchBoardShouldUpdateNumberOfVotesWithoutProvidingName() {
        // given
        final var uid = "uid101";
        final var email = "username@test.pl";
        final var user = new User(uid, email, "displayName", Set.of());
        final var board = buildBoard(user);
        final var id = 10;
        final var boardPatchDto = new BoardPatchDto(null, 1500);

        // when
        when(boardRepository.save(any(Board.class))).thenReturn(board);
        when(boardRepository.findById(id)).thenReturn(Optional.of(board));

        // then
        final var boardDtoResult = boardService.patchBoard(id, boardPatchDto, email);

        assertEquals(BoardDto.fromModel(board), boardDtoResult);
        verify(boardRepository).findById(id);
        verify(boardRepository).save(any(Board.class));
    }

    @Test
    @DisplayName("assignUsersToBoard should throw 404 when user does not exist")
    void assignUsersToBoardShouldThrowNotFoundWhenUserDoesNotExist() {
        //given
        final int boardId = 1;
        final List<String> usersEmails = List.of();
        final String email = "testemail@example.com";

        //when
        when(userRepository.findUserByEmail(email)).thenReturn(Optional.empty());

        //then
        assertThrows(NotFoundException.class, () -> boardService.assignUsersToBoard(boardId, usersEmails, email));
    }

    @Test
    @DisplayName("assignUsersToBoard should throw 404 when user does not exist")
    void assignUsersToBoardShouldThrowNotFoundWhenBoardDoesNotExist() {
        //given
        final int boardId = 1;
        final List<String> usersEmails = List.of();
        final String email = "testemail@example.com";
        final User user = new User("123", email, "test name", Set.of());

        //when
        when(userRepository.findUserByEmail(email)).thenReturn(Optional.of(user));
        when(boardRepository.findById(boardId)).thenReturn(Optional.empty());

        //then
        assertThrows(NotFoundException.class, () -> boardService.assignUsersToBoard(boardId, usersEmails, email));
    }

    @Test
    @DisplayName("assignUsersToBoard should throw 400 when user is not an owner")
    void assignUsersToBoardShouldThrowBadRequestWhenUserIsNotOwner() {
        //given
        final int boardId = 1;
        final List<String> usersEmails = List.of();
        final String email = "testemail@example.com";
        final User user = new User("123", email, "test name", Set.of());
        final User boardOwner = new User("1234", "testemail1@example.com", "test name", Set.of());

        //when
        when(userRepository.findUserByEmail(email)).thenReturn(Optional.of(user));
        when(boardRepository.findById(boardId)).thenReturn(Optional.of(buildBoard(boardOwner)));

        //then
        assertThrows(BadRequestException.class, () -> boardService.assignUsersToBoard(boardId, usersEmails, email));
    }

    @Test
    @DisplayName("assignUsersToBoard should return 201")
    void assignUsersToBoardShouldReturnOk() {
        //given
        final int boardId = 1;
        final List<String> usersEmails = List.of("testemail@example.com", "testfalseemail@example.com");
        final String ownerEmail = "owner@example.com";
        final String displayName = "testDisplayName";
        final User owner = new User("123", ownerEmail, displayName, Set.of());
        final User user = new User("1234", usersEmails.get(0), displayName, new HashSet<>());
        final Board board = buildBoard(owner);

        //when
        when(userRepository.findUserByEmail(ownerEmail)).thenReturn(Optional.of(owner));
        when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
        when(userRepository.findUserByEmail(usersEmails.get(0))).thenReturn(Optional.of(user));
        when(userRepository.findUserByEmail(usersEmails.get(1))).thenReturn(Optional.empty());
        final BoardFailedEmailsDto boardFailedEmailsDto =
                boardService.assignUsersToBoard(boardId, usersEmails, ownerEmail);

        //then
        verify(boardRepository).save(any(Board.class));
        assertEquals(boardFailedEmailsDto.getFailedEmails().get(0), usersEmails.get(1));
    }

    private Board buildBoard(final User user) {
        return Board.builder()
                .id(10)
                .name("My first board.")
                .state(EnumStateDto.CREATED)
                .creator(user)
                .users(Set.of())
                .build();
    }
}
