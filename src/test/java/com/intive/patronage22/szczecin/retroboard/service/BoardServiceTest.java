package com.intive.patronage22.szczecin.retroboard.service;

import com.intive.patronage22.szczecin.retroboard.dto.BoardCardsColumn;
import com.intive.patronage22.szczecin.retroboard.dto.BoardDataDto;
import com.intive.patronage22.szczecin.retroboard.dto.BoardDetailsDto;
import com.intive.patronage22.szczecin.retroboard.dto.BoardDto;
import com.intive.patronage22.szczecin.retroboard.dto.BoardPatchDto;
import com.intive.patronage22.szczecin.retroboard.dto.EnumStateDto;
import com.intive.patronage22.szczecin.retroboard.exception.BadRequestException;
import com.intive.patronage22.szczecin.retroboard.exception.NotAcceptableException;
import com.intive.patronage22.szczecin.retroboard.exception.NotFoundException;
import com.intive.patronage22.szczecin.retroboard.model.Board;
import com.intive.patronage22.szczecin.retroboard.model.BoardCard;
import com.intive.patronage22.szczecin.retroboard.model.BoardCardAction;
import com.intive.patronage22.szczecin.retroboard.model.User;
import com.intive.patronage22.szczecin.retroboard.repository.BoardCardsRepository;
import com.intive.patronage22.szczecin.retroboard.repository.BoardRepository;
import com.intive.patronage22.szczecin.retroboard.repository.UserRepository;
import com.intive.patronage22.szczecin.retroboard.validation.BoardValidator;
import org.hibernate.annotations.NotFound;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
        final User user = new User(uid, email, "john14", Set.of(), Set.of());
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

        final User user = new User(uid, email, "josef14", Set.of(), Set.of());

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
        final User user = new User("123", email, displayName, Set.of(),Set.of());

        //when
        when(userRepository.findUserByEmail(email)).thenReturn(Optional.of(user));
        when(boardRepository.findById(boardId)).thenReturn(Optional.empty());

        //then
        assertThrows(NotFoundException.class, () -> boardService.getBoardDataById(boardId, email));
    }

    @Test
    @DisplayName("getBoardDataById should throw 400 when user has no access to view board")
    void getBoardDataByIdShouldThrowBadRequestWhenUserHasNoAccessToBoard() {
        //given
        final int boardId = 1;
        final String email = "testemail@example.com";
        final String displayName = "test12";
        final User user = new User("123", email, displayName, Set.of(),Set.of());
        final User assignUser = new User("1234", "assignUser@test.pl", "test1", Set.of(),Set.of());
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
    @DisplayName("getBoardDataById should return board data")
    void getBoardDataByIdShouldReturnBoardData() {
        //given
        final int boardId = 1;
        final String email = "testemail@example.com";
        final String displayName = "testDisplayName";
        final User user = new User("123", email, displayName, Set.of(),Set.of());
        final User assignUser = new User("1234", "assignUser", "test1", Set.of(),Set.of());
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

        final BoardDataDto boardDataDto = boardService.getBoardDataById(boardId, email);

        //then
        assertEquals(boardDataDto.getBoard().getId(), board.getId());
        assertEquals(boardDataDto.getBoard().getState(), board.getState());
        assertEquals(boardDataDto.getBoard().getName(), board.getName());
        assertEquals(boardDataDto.getBoard().getNumberOfVotes(), board.getMaximumNumberOfVotes());
        assertEquals(boardDataDto.getColumns().get(0).getName(), BoardCardsColumn.SUCCESS.name());
        assertEquals(boardDataDto.getColumns().get(0).getId(), BoardCardsColumn.SUCCESS.getColumnId());
        assertEquals(boardDataDto.getColumns().get(0).getPosition(), BoardCardsColumn.SUCCESS.getColumnId());
        assertEquals(boardDataDto.getColumns().get(0).getColour(), BoardCardsColumn.SUCCESS.getColour());
        assertTrue(boardDataDto.getUsers().toString().contains(user.getEmail()));
        assertTrue(boardDataDto.getUsers().toString().contains(user.getUid()));
        assertTrue(boardDataDto.getUsers().toString().contains(assignUser.getEmail()));
        assertTrue(boardDataDto.getUsers().toString().contains(assignUser.getUid()));
    }

    @Test
    @DisplayName("getBoardDetailsById should throw 400 when user does not exist")
    void getBoardDetailsByIdShouldThrowBadRequestWhenUserDoesNotExist() {
        //given
        final int boardId = 1;
        final String email = "testemail@example.com";

        //when
        when(userRepository.findUserByEmail(email)).thenReturn(Optional.empty());

        //then
        assertThrows(BadRequestException.class, () -> boardService.getBoardDetailsById(boardId, email));
    }

    @Test
    @DisplayName("getBoardDetailsById should throw 404 when board does not exist")
    void getBoardDetailsByIdShouldThrowNotFoundWhenBoardDoesNotExist() {
        //given
        final int boardId = 1;
        final String email = "testemail@example.com";
        final String displayName = "test";
        final User user = new User("123", email, displayName, Set.of(),Set.of());

        //when
        when(userRepository.findUserByEmail(email)).thenReturn(Optional.of(user));
        when(boardRepository.findById(boardId)).thenReturn(Optional.empty());

        //then
        assertThrows(NotFoundException.class, () -> boardService.getBoardDetailsById(boardId, email));
    }

    @Test
    @DisplayName("getBoardDetailsById should throw 400 when user has no access to the board")
    void getBoardDetailsByIdShouldThrowBadRequestWhenUserHasNoAccessToBoard() {
        //given
        final int boardId = 1;
        final String email = "testemail@example.com";
        final String displayName = "test12";
        final User user = new User("123", email, displayName, Set.of(),Set.of());
        final User assignUser = new User("1234", "assignUser@test.pl", "test1", Set.of(),Set.of());
        final Board board = Board.builder()
                .id(1)
                .name("board name")
                .state(EnumStateDto.CREATED)
                .creator(user)
                .users(Set.of(assignUser))
                .boardCards(Set.of())
                .build();

        //when
        when(userRepository.findUserByEmail(email)).thenReturn(Optional.of(user));
        when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
        when(boardRepository.findBoardByIdAndCreatorOrAssignedUser(boardId, user)).thenReturn(Optional.empty());

        //then
        assertThrows(BadRequestException.class, () -> boardService.getBoardDetailsById(boardId, email));
    }

    @Test
    @DisplayName("getBoardDetailsById should return board cards created by user when board state is created")
    void getBoardDetailsByIdShouldReturnUserBoardCardsWhenBoardStateIsCreated() {
        //given
        final int boardId = 1;
        final String userEmail = "testemail@example.com";
        final String assignedUserEmail = "test@example.com";
        final String displayName = "testDisplayName";
        final User user = new User("123", userEmail, displayName, Set.of(),Set.of());
        final User assignedUser = new User("1234", assignedUserEmail, displayName, Set.of(),Set.of());
        final Board board = Board.builder()
                .id(boardId)
                .name("board name")
                .state(EnumStateDto.CREATED)
                .creator(user)
                .users(Set.of(assignedUser))
                .boardCards(Set.of())
                .build();
        final BoardCardAction successAction = new BoardCardAction(6, null, "happy");
        final BoardCardAction failureAction = new BoardCardAction(7, null, "help");
        final BoardCardAction kudosAction = new BoardCardAction(8, null, "awesome");
        final BoardCard successBoardCard =
                new BoardCard(3, board, "success", BoardCardsColumn.SUCCESS, user, List.of(successAction));
        final BoardCard failureBoardCard =
                new BoardCard(4, board, "failure", BoardCardsColumn.FAILURES, user, List.of(failureAction));
        final BoardCard kudosBoardCard =
                new BoardCard(5, board, "kudos", BoardCardsColumn.KUDOS, user, List.of(kudosAction));
        final BoardCard assignedUserCard =
                new BoardCard(9, board, "success", BoardCardsColumn.SUCCESS, assignedUser, List.of());

        //when
        when(userRepository.findUserByEmail(userEmail)).thenReturn(Optional.of(user));
        when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
        when(boardRepository.findBoardByIdAndCreatorOrAssignedUser(boardId, user)).thenReturn(Optional.of(board));
        when(boardCardsRepository.findAllByBoardIdAndCreatorOrderByIdAsc(boardId, user)).thenReturn(
                List.of(successBoardCard, failureBoardCard, kudosBoardCard));

        final List<BoardDetailsDto> boardDetailsDto = boardService.getBoardDetailsById(boardId, userEmail);

        //then
        assertEquals(boardDetailsDto.get(0).getId(), BoardCardsColumn.SUCCESS.getColumnId());
        assertEquals(boardDetailsDto.get(0).getBoardCards().get(0).getId(), successBoardCard.getId());
        assertEquals(boardDetailsDto.get(0).getBoardCards().get(0).getCardText(), successBoardCard.getText());
        assertEquals(boardDetailsDto.get(0).getBoardCards().get(0).getBoardCardCreator(),
                successBoardCard.getCreator().getEmail());
        assertEquals(boardDetailsDto.get(0).getBoardCards().get(0).getActionTexts().get(0),
                successBoardCard.getBoardCardActions().get(0).getText());

        assertEquals(boardDetailsDto.get(1).getId(), BoardCardsColumn.FAILURES.getColumnId());
        assertEquals(boardDetailsDto.get(1).getBoardCards().get(0).getId(), failureBoardCard.getId());
        assertEquals(boardDetailsDto.get(1).getBoardCards().get(0).getCardText(), failureBoardCard.getText());
        assertEquals(boardDetailsDto.get(1).getBoardCards().get(0).getBoardCardCreator(),
                failureBoardCard.getCreator().getEmail());
        assertEquals(boardDetailsDto.get(1).getBoardCards().get(0).getActionTexts().get(0),
                failureBoardCard.getBoardCardActions().get(0).getText());

        assertEquals(boardDetailsDto.get(2).getId(), BoardCardsColumn.KUDOS.getColumnId());
        assertEquals(boardDetailsDto.get(2).getBoardCards().get(0).getId(), kudosBoardCard.getId());
        assertEquals(boardDetailsDto.get(2).getBoardCards().get(0).getCardText(), kudosBoardCard.getText());
        assertEquals(boardDetailsDto.get(2).getBoardCards().get(0).getBoardCardCreator(),
                kudosBoardCard.getCreator().getEmail());
        assertEquals(boardDetailsDto.get(2).getBoardCards().get(0).getActionTexts().get(0),
                kudosBoardCard.getBoardCardActions().get(0).getText());

        assertFalse(boardDetailsDto.toString().contains(assignedUserCard.getCreator().getEmail()));
        assertFalse(boardDetailsDto.toString().contains(assignedUserCard.getId().toString()));
    }

    @Test
    @DisplayName("getBoardDetailsById should return all board cards when board state is other than created")
    void getBoardDetailsByIdShouldReturnAllBoardCardsWhenBoardStateIsOtherThanCreated() {
        //given
        final int boardId = 1;
        final String userEmail = "testemail@example.com";
        final String assignedUserEmail = "test@example.com";
        final String displayName = "testDisplayName";
        final User user = new User("123", userEmail, displayName, Set.of(),Set.of());
        final User assignedUser = new User("1234", assignedUserEmail, displayName, Set.of(), Set.of());
        final Board board = Board.builder()
                .id(boardId)
                .name("board name")
                .state(EnumStateDto.ACTIONS)
                .creator(user)
                .users(Set.of(assignedUser))
                .boardCards(Set.of())
                .build();
        final BoardCardAction successAction = new BoardCardAction(6, null, "happy");
        final BoardCardAction failureAction = new BoardCardAction(7, null, "help");
        final BoardCardAction kudosAction = new BoardCardAction(8, null, "awesome");
        final BoardCard successBoardCard =
                new BoardCard(3, board, "success", BoardCardsColumn.SUCCESS, user, List.of(successAction));
        final BoardCard failureBoardCard =
                new BoardCard(4, board, "failure", BoardCardsColumn.FAILURES, user, List.of(failureAction));
        final BoardCard kudosBoardCard =
                new BoardCard(5, board, "kudos", BoardCardsColumn.KUDOS, user, List.of(kudosAction));
        final BoardCard assignedUserCard =
                new BoardCard(9, board, "success", BoardCardsColumn.SUCCESS, assignedUser, List.of());

        //when
        when(userRepository.findUserByEmail(userEmail)).thenReturn(Optional.of(user));
        when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
        when(boardRepository.findBoardByIdAndCreatorOrAssignedUser(boardId, user)).thenReturn(Optional.of(board));
        when(boardCardsRepository.findAllByBoardIdOrderByIdAsc(boardId)).thenReturn(
                List.of(successBoardCard, assignedUserCard, failureBoardCard, kudosBoardCard));

        final List<BoardDetailsDto> boardDetailsDto = boardService.getBoardDetailsById(boardId, userEmail);

        //then
        assertTrue(boardDetailsDto.toString().contains(String.valueOf(BoardCardsColumn.SUCCESS.getColumnId())));
        assertTrue(boardDetailsDto.toString().contains(successBoardCard.getId().toString()));
        assertTrue(boardDetailsDto.toString().contains(successBoardCard.getText()));
        assertTrue(boardDetailsDto.toString().contains(successBoardCard.getCreator().getEmail()));
        assertTrue(boardDetailsDto.toString().contains(successBoardCard.getBoardCardActions().get(0).getText()));

        assertTrue(boardDetailsDto.toString().contains(assignedUserCard.getId().toString()));
        assertTrue(boardDetailsDto.toString().contains(assignedUserCard.getText()));
        assertTrue(boardDetailsDto.toString().contains(assignedUserCard.getCreator().getEmail()));

        assertTrue(boardDetailsDto.toString().contains(String.valueOf(BoardCardsColumn.FAILURES.getColumnId())));
        assertTrue(boardDetailsDto.toString().contains(failureBoardCard.getId().toString()));
        assertTrue(boardDetailsDto.toString().contains(failureBoardCard.getText()));
        assertTrue(boardDetailsDto.toString().contains(failureBoardCard.getCreator().getEmail()));
        assertTrue(boardDetailsDto.toString().contains(failureBoardCard.getBoardCardActions().get(0).getText()));

        assertTrue(boardDetailsDto.toString().contains(String.valueOf(BoardCardsColumn.KUDOS.getColumnId())));
        assertTrue(boardDetailsDto.toString().contains(kudosBoardCard.getId().toString()));
        assertTrue(boardDetailsDto.toString().contains(kudosBoardCard.getText()));
        assertTrue(boardDetailsDto.toString().contains(kudosBoardCard.getCreator().getEmail()));
        assertTrue(boardDetailsDto.toString().contains(kudosBoardCard.getBoardCardActions().get(0).getText()));
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
        final User userOwner = new User(uidOwner, emailOwner, "displayName", Set.of(),Set.of());
        final User user = new User(uid, email, "displayName", Set.of(),Set.of());
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
        final User userOwner = new User(uidOwner, emailOwner, "displayName", Set.of(),Set.of());
        final User user = new User(uid, email, "displayName", Set.of(), Set.of());
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
        final var user = new User(uid, email, "displayName", Set.of(), Set.of());
        final var board = buildBoard(user, EnumStateDto.CREATED, 10, Set.of());
        when(boardRepository.findById(board.getId())).thenReturn(Optional.of(board));
        final var boardPatchDto = new BoardPatchDto("testboard", 1500);


        // when & then
        assertThrows(BadRequestException.class,
                () -> boardService.patchBoard(board.getId(), boardPatchDto, "some@test.pl"));
    }

    @Test
    void patchBoardShouldUpdateNameAndNumberOfVotesFields() {
        // given
        final var uid = "uid101";
        final var email = "username@test.pl";
        final var boardName = "My first board.";
        final var userOwner = new User(uid, email, "displayName", Set.of(), Set.of());
        final var board = buildBoard(userOwner, EnumStateDto.CREATED, 10, Set.of());
        final var boardPatchDto = new BoardPatchDto(boardName, 1500);

        // when
        when(boardRepository.save(any(Board.class))).thenReturn(board);
        when(boardRepository.findById(board.getId())).thenReturn(Optional.of(board));

        // then
        final var boardDtoResult = boardService.patchBoard(board.getId(), boardPatchDto, email);

        assertEquals(BoardDto.fromModel(board), boardDtoResult);
        verify(boardRepository).findById(board.getId());
        verify(boardRepository).save(any(Board.class));
    }

    @Test
    void patchBoardShouldUpdateNumberOfVotesWithoutProvidingName() {
        // given
        final var uid = "uid101";
        final var email = "username@test.pl";
        final var user = new User(uid, email, "displayName", Set.of(), Set.of());
        final var board = buildBoard(user, EnumStateDto.CREATED, 10, Set.of());
        final var boardPatchDto = new BoardPatchDto(null, 1500);

        // when
        when(boardRepository.save(any(Board.class))).thenReturn(board);
        when(boardRepository.findById(board.getId())).thenReturn(Optional.of(board));

        // then
        final var boardDtoResult = boardService.patchBoard(board.getId(), boardPatchDto, email);

        assertEquals(BoardDto.fromModel(board), boardDtoResult);
        verify(boardRepository).findById(board.getId());
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
        final User user = new User("123", email, "test name", Set.of(), Set.of());

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
        final User user = new User("123", email, "test name", Set.of(), Set.of());
        final User boardOwner = new User("1234", "testemail1@example.com", "test name", Set.of(), Set.of());

        //when
        when(userRepository.findUserByEmail(email)).thenReturn(Optional.of(user));
        when(boardRepository.findById(boardId))
                .thenReturn(Optional.of(buildBoard(boardOwner, EnumStateDto.CREATED, 10, Set.of())));

        //then
        assertThrows(BadRequestException.class, () -> boardService.assignUsersToBoard(boardId, usersEmails, email));
    }

    @Test
    @DisplayName("assignUsersToBoard should return List of unsuccessfully emails ")
    void assignUsersToBoardShouldReturnFailedEmails() throws JSONException {
        //given
        final int boardId = 1;
        final List<String> usersEmails = List.of("testemail@example.com", "testfalseemail@example.com", "test@123.pl");
        final String ownerEmail = "owner@example.com";
        final String displayName = "testDisplayName";
        final User owner = new User("123", ownerEmail, displayName, Set.of(), Set.of());
        final User userToAssign = new User("126", usersEmails.get(0), displayName, new HashSet<>(), Set.of());
        final User existingUser = new User("1234", "test@123.pl", displayName, new HashSet<>(), Set.of());
        final Set<User> boardUsers = new HashSet<>(List.of(existingUser));
        final List<User> existingUsers = List.of(userToAssign, existingUser);

        final Board board = buildBoard(owner, EnumStateDto.CREATED, 10, boardUsers);

        //when
        when(userRepository.findUserByEmail(ownerEmail)).thenReturn(Optional.of(owner));
        when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
        when(userRepository.findAllByEmailIn(usersEmails)).thenReturn(existingUsers);
        final JSONArray failedEmails = new JSONArray(boardService.assignUsersToBoard(boardId, usersEmails, ownerEmail));

        //then
        final ArgumentCaptor<Board> usersCaptor = ArgumentCaptor.forClass(Board.class);
        Mockito.verify(boardRepository).save(usersCaptor.capture());

        final Board savedBoard = usersCaptor.getValue();
        final Set<User> allBoardUsers = savedBoard.getUsers();

        assertEquals(allBoardUsers.size(), 2);
        assertTrue(allBoardUsers.contains(existingUser));
        assertTrue(allBoardUsers.contains(userToAssign));

        verify(boardRepository).save(any(Board.class));
        assertEquals(failedEmails.get(0), usersEmails.get(1));
    }

    @Test
    void patchBoardShouldReturnBadRequestWhenBoardStateIsNotCreated() {
        // given
        final var uid = "1234";
        final var email = "John@test.pl";
        final var user = new User(uid, email, "john14", Set.of(), Set.of());
        final var board = buildBoard(user, EnumStateDto.VOTING, 10, Set.of());
        final var boardPatchDto = new BoardPatchDto("testboard", 1500);
        when(userRepository.findUserByEmail(email)).thenReturn(Optional.of(user));
        when(boardRepository.findById(board.getId())).thenReturn(Optional.of(board));

        // when & then
        assertThrows(BadRequestException.class,
                () -> boardService.patchBoard(board.getId(), boardPatchDto, email));
    }

    @Test
    void patchBoardShouldReturnOkWhenStateIsCreated() {
        // given
        final var uid = "1234";
        final var email = "John@test.pl";
        final var user = new User(uid, email, "john14", Set.of(), Set.of());
        final var board = buildBoard(user, EnumStateDto.CREATED, 10, Set.of());
        final var boardPatchDto = new BoardPatchDto("testboard", 1500);
        when(userRepository.findUserByEmail(email)).thenReturn(Optional.of(user));
        when(boardRepository.findById(board.getId())).thenReturn(Optional.of(board));

        // when & then
        final var boardDtoResult = boardService.patchBoard(board.getId(), boardPatchDto, email);

        assertEquals(BoardDto.fromModel(board), boardDtoResult);
        verify(boardRepository).findById(board.getId());
        verify(boardRepository).save(any(Board.class));
    }

    @Test
    void getUserBoardsShouldReturnTwoBoardsIfUserCreatedOneAndIsAssignedToTheOtherOne() {
        // given
        final var uid1 = "uid101";
        final var email1 = "username@test.pl";
        final var uid2 = "uid102";
        final var email2 = "username2@test.pl";
        final var user1 = new User(uid1, email1, "displayName1", Set.of(), Set.of());
        final var user2 = new User(uid2, email2, "displayName2", Set.of(), Set.of());
        final var boardCreated = buildBoard(user1, EnumStateDto.CREATED, 10, Set.of());
        final var boardAssigned = buildBoard(user2, EnumStateDto.CREATED, 11, Collections.singleton(user1));
        when(userRepository.findUserByEmail(email1)).thenReturn(Optional.of(user1));
        user1.setUserBoards(Collections.singleton(boardAssigned));
        user1.setCreatedBoards(Collections.singleton(boardCreated));

        //when
        final List<BoardDto> boards = boardService.getUserBoards(email1);

        //then
        assertThat(boards, hasSize(2));
    }

    @Test
    void removeAssignedUserShouldThrowNotFoundWhenBoardOwnerTriesToRemoveUserNotAssignedToTheBoard() {
        // given
        final String uid = "12345";
        final String email = "boardOwner@test.pl";
        final Integer boardId = 10;

        final User notAssignedUser = new User(uid, "test@test.com", "some_user", Set.of(), Set.of());
        final User boardOwner = new User("123", email, "board_owner", Set.of(), Set.of());
        final Board board = buildBoard(boardOwner, EnumStateDto.CREATED, boardId, Set.of());

        // when
        when(userRepository.findById(uid)).thenReturn(Optional.of(notAssignedUser));
        when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));

        // then
        final NotFoundException exception = assertThrows(
                NotFoundException.class, () -> boardService.removeUserAssignedToTheBoard(uid, boardId, email));

        assertEquals("User is not assigned to the Board.", exception.getMessage());
    }

    @Test
    void removeAssignedUserShouldThrowNotFoundWhenUserTriesToRemoveHimselfAndHeIsNotAssignedToTheBoard() {
        // given
        final String uid = "12345";
        final String email = "someUser@test.pl";
        final Integer boardId = 10;

        final User notAssignedUser = new User(uid, email, "some_user", Set.of(), Set.of());
        final User boardOwner = new User("123", "boardOwner@test.pl", "board_owner", Set.of(), Set.of());
        final Board board = buildBoard(boardOwner, EnumStateDto.CREATED, boardId, Set.of());

        // when
        when(userRepository.findById(uid)).thenReturn(Optional.of(notAssignedUser));
        when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));

        // then
        final NotFoundException exception = assertThrows(
                NotFoundException.class, () -> boardService.removeUserAssignedToTheBoard(uid, boardId, email));

        assertEquals("User is not assigned to the Board.", exception.getMessage());
    }

    @Test
    @DisplayName("Remove assigned user should throw NotFound when user not exists")
    void removeAssignedUserShouldThrowNotFoundWhenUserNotExists() {
        //given
        final String uid = "12345";
        final String email = "someUser@test.pl";
        final Integer boardId = 10;

        //when
        when(userRepository.findById(uid)).thenReturn(Optional.empty());

        //then
        final NotFoundException exception = assertThrows(
                NotFoundException.class, () -> boardService.removeUserAssignedToTheBoard(uid, boardId, email));

        assertEquals("User is not found", exception.getMessage());
    }

    @Test
    @DisplayName("Remove assigned user should throw BadRequest when currently logged user is not board owner and he tries to delete other user")
    void removeAssignedUserShouldThrowBadRequestWhenCurrentlyLoggedUserIsNotBoardOwnerAndHeTriesToDeleteOtherUser() {
        //given
        final String uid = "12345";
        final String email = "currentlyLogged@test.pl";
        final Integer boardId = 10;

        final User boardOwner = new User("123", "boardOwner@test1.com", "board_owner", Set.of(), Set.of());
        final User userToRemove = new User(uid, "test3@test3.com", "userTest", Set.of(), Set.of());
        final Board board = buildBoard(boardOwner, EnumStateDto.CREATED, boardId, Set.of(userToRemove));

        //when
        when(userRepository.findById(uid)).thenReturn(Optional.of(userToRemove));
        when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));

        //then
        final BadRequestException exception = assertThrows(
                BadRequestException.class, () -> boardService.removeUserAssignedToTheBoard(uid, boardId, email));

        assertEquals("Currently logged user is not board owner or user tries to delete other user",
                exception.getMessage());
    }

    @Test
    @DisplayName("Remove assigned user should throw BadRequest when board owner tries to self delete")
    void removeAssignedUserShouldThrowBadRequestWhenBoardOwnerTriesToSelfDelete() {
        //given
        final String uid = "12345";
        final String email = "boardOwner@test1.com";
        final Integer boardId = 10;

        final User boardOwner = new User(uid, email, "board_owner", Set.of(), Set.of());
        final Board board = buildBoard(boardOwner, EnumStateDto.CREATED, boardId, Set.of());

        //when
        when(userRepository.findById(uid)).thenReturn(Optional.of(boardOwner));
        when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));

        //then
        final BadRequestException exception = assertThrows(
                BadRequestException.class, () -> boardService.removeUserAssignedToTheBoard(uid, boardId, email));

        assertEquals("User is the board owner.", exception.getMessage());
    }

    @Test
    @DisplayName("Remove assigned user should remove assigned user when board owner tries to delete other user")
    void removeAssignedUserShouldRemoveWhenBoardOwnerTriesToDeleteOtherUser() {
        //given
        final String uid = "456";
        final String email = "boardOwner@test1.com";
        final Integer boardId = 10;

        final User boardOwner = new User("12345", email, "board_owner", Set.of(), Set.of());
        final User userToRemove = new User(uid, "test2@test2.com", "userTest", Set.of(), Set.of());
        final Board board = buildBoard(boardOwner, EnumStateDto.CREATED, boardId, new HashSet<>());
        board.getUsers().add(userToRemove);

        //when
        when(userRepository.findById(uid)).thenReturn(Optional.of(userToRemove));
        when(boardRepository.findById(board.getId())).thenReturn(Optional.of(board));

        //then
        boardService.removeUserAssignedToTheBoard(uid, boardId, email);

        assertEquals(boardOwner, board.getCreator());
        assertFalse(board.getUsers().contains(userToRemove));
        assertTrue(board.getUsers().isEmpty());
    }

    @Test
    @DisplayName("Remove assigned user should remove assigned user when currently logged user is not board owner and tries to self delete")
    void removeAssignedUserShouldRemoveWhenCurrentlyLoggedUserIsNotBoardOwnerAndTriesToSelfDelete() {
        //given
        final String uid = "789";
        final String email = "currentlyLogged@test.pl";
        final Integer boardId = 10;

        final User boardOwner = new User("12345", "boardOwner@test1.com", "board_owner", Set.of(), Set.of());
        final User currentlyLogged = new User(uid, email, "currently_logged", Set.of(), Set.of());
        final Board board = buildBoard(boardOwner, EnumStateDto.CREATED, boardId, new HashSet<>());
        board.getUsers().add(currentlyLogged);

        //when
        when(userRepository.findById(uid)).thenReturn(Optional.of(currentlyLogged));
        when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));

        //then
        boardService.removeUserAssignedToTheBoard(uid, boardId, email);

        assertNotEquals(currentlyLogged, board.getCreator());
        assertFalse(board.getUsers().contains(currentlyLogged));
        assertTrue(board.getUsers().isEmpty());
    }

    @Test
    @DisplayName("When nextState is used - it will change state from Created to next state which is Voting and saves board details.")
    void nextStateSavesDataAndChangesStateProperly() {

        // given
        final var uid = "uid101";
        final var email = "username@test.pl";
        final var user = new User(uid, email, "displayName", Set.of(), Set.of());
        final var board = buildBoard(user, EnumStateDto.CREATED, 10, Set.of());
        when(boardRepository.save(any(Board.class))).thenReturn(board);
        when(boardRepository.findById(board.getId())).thenReturn(Optional.of(board));
        when(userRepository.findUserByEmail(email)).thenReturn(Optional.of(user));

        // when
        final var boardDtoResult = boardService.nextState(board.getId(), email);

        // then
        assertEquals(boardDtoResult.getBoard().getState(), EnumStateDto.VOTING);
        verify(boardRepository).findById(board.getId());
        verify(boardRepository).save(any(Board.class));
    }

    @Test
    @DisplayName("When nextState is used on a board with EnumStateDto set to DONE - Not Acceptable Exception is thrown.")
    void nextStateThrowsNotAcceptableIfStateIsDone() {

        // given
        final var uid = "uid101";
        final var email = "username@test.pl";
        final var user = new User(uid, email, "displayName", Set.of(), Set.of());
        final var board = buildBoard(user, EnumStateDto.DONE, 10, Set.of());
        when(boardRepository.save(any(Board.class))).thenReturn(board);
        when(boardRepository.findById(board.getId())).thenReturn(Optional.of(board));
        when(userRepository.findUserByEmail(email)).thenReturn(Optional.of(user));

        // when
        final NotAcceptableException exception = assertThrows(
                NotAcceptableException.class, () -> boardService.nextState(board.getId(),email));

        // then
        assertEquals("Already in last state", exception.getMessage());
    }

    @Test
    @DisplayName("When nextState is used on a board which does not exist - Not Found Exception is thrown.")
    void nextStateThrowsNotFoundIfBoardIsNotFound() {

        // given
        final var uid = "uid101";
        final var email = "username@test.pl";
        final var user = new User(uid, email, "displayName", Set.of(), Set.of());
        when(userRepository.findUserByEmail(email)).thenReturn(Optional.of(user));

        // when
        final NotFoundException exception = assertThrows(
                NotFoundException.class, () -> boardService.nextState(1,email));

        // then
        assertEquals("Board not found", exception.getMessage());
    }

    @Test
    @DisplayName("When nextState is used on a board which the user is not a creator of - Not Found Exception is thrown.")
    void nextStateThrowsNotFoundIfUserIsNotAnOwner() {

        // given
        final var uid1 = "uid101";
        final var email1 = "username1@test.pl";
        final var uid2 = "uid102";
        final var email2 = "username2@test.pl";
        final var user1 = new User(uid1, email1, "displayName", Set.of(), Set.of());
        final var user2 = new User(uid2, email2, "displayName2", Set.of(), Set.of());
        final var board = buildBoard(user1, EnumStateDto.CREATED, 10, Set.of());
        when(boardRepository.save(any(Board.class))).thenReturn(board);
        when(boardRepository.findById(board.getId())).thenReturn(Optional.of(board));
        when(userRepository.findUserByEmail(email1)).thenReturn(Optional.of(user1));
        when(userRepository.findUserByEmail(email2)).thenReturn(Optional.of(user2));

        // when
        final NotFoundException exception = assertThrows(
                NotFoundException.class, () -> boardService.nextState(board.getId(),email2));

        // then
        assertEquals("User is not the board owner.", exception.getMessage());
    }

    @Test
    @DisplayName("When nextState on a board which maximumNumberOfVotes is set to 0 - Bad Request Exception is thrown.")
    void nextStateThrowsBadRequestWhenNumberOfVotesIsNotSet() {

        // given
        final var uid = "uid101";
        final var email = "username@test.pl";
        final var user = new User(uid, email, "displayName", Set.of(), Set.of());
        final var board = buildBoard(user, EnumStateDto.CREATED, 10, Set.of());
        board.setMaximumNumberOfVotes(0);
        when(boardRepository.save(any(Board.class))).thenReturn(board);
        when(boardRepository.findById(board.getId())).thenReturn(Optional.of(board));
        when(userRepository.findUserByEmail(email)).thenReturn(Optional.of(user));

        // when
        final BadRequestException exception = assertThrows(
                BadRequestException.class, () -> boardService.nextState(board.getId(),email));

        // then
        assertEquals("Number of votes not set!", exception.getMessage());
    }

    private Board buildBoard(final User user, final EnumStateDto state, final int id, final Set<User> users) {
        return Board.builder()
                .id(id)
                .name("My first board.")
                .state(state)
                .creator(user)
                .users(users)
                .maximumNumberOfVotes(3)
                .build();
    }
}
