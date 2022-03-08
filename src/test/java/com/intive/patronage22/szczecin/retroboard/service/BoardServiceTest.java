package com.intive.patronage22.szczecin.retroboard.service;

import com.intive.patronage22.szczecin.retroboard.dto.BoardCardsColumn;
import com.intive.patronage22.szczecin.retroboard.dto.BoardDataDto;
import com.intive.patronage22.szczecin.retroboard.dto.BoardDetailsDto;
import com.intive.patronage22.szczecin.retroboard.dto.BoardDto;
import com.intive.patronage22.szczecin.retroboard.dto.BoardPatchDto;
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
import com.intive.patronage22.szczecin.retroboard.validation.BoardValidator;
import org.json.JSONArray;
import org.json.JSONException;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
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
    @DisplayName("getBoardDataById should throw 400 when user has no access to view board")
    void getBoardDataByIdShouldThrowBadRequestWhenUserHasNoAccessToBoard() {
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
    @DisplayName("getBoardDataById should return board data")
    void getBoardDataByIdShouldReturnBoardData() {
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

        final BoardDataDto boardDataDto = boardService.getBoardDataById(boardId, email);

        //then
        assertEquals(boardDataDto.getBoard().getId(), board.getId());
        assertEquals(boardDataDto.getBoard().getState(), board.getState());
        assertEquals(boardDataDto.getBoard().getName(), board.getName());
        assertEquals(boardDataDto.getBoard().getNumberOfVotes(), board.getMaximumNumberOfVotes());
        assertEquals(boardDataDto.getColumns().get(0).getName(), BoardCardsColumn.SUCCESS.name());
        assertEquals(boardDataDto.getColumns().get(0).getId(), BoardCardsColumn.SUCCESS.orderNumber);
        assertEquals(boardDataDto.getColumns().get(0).getPosition(), BoardCardsColumn.SUCCESS.orderNumber);
        assertEquals(boardDataDto.getColumns().get(0).getColour(), BoardCardsColumn.SUCCESS.colour);
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
        final User user = new User("123", email, displayName, Set.of());

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
        final User user = new User("123", email, displayName, Set.of());
        final User assignUser = new User("1234", "assignUser@test.pl", "test1", Set.of());
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
        final User user = new User("123", userEmail, displayName, Set.of());
        final User assignedUser = new User("1234", assignedUserEmail, displayName, Set.of());
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
        when(boardCardsRepository.findAllByCreatorOrderByIdAsc(user)).thenReturn(
                List.of(successBoardCard, failureBoardCard, kudosBoardCard));

        final List<BoardDetailsDto> boardDetailsDto = boardService.getBoardDetailsById(boardId, userEmail);

        //then
        assertEquals(boardDetailsDto.get(0).getId(), BoardCardsColumn.SUCCESS.orderNumber);
        assertEquals(boardDetailsDto.get(0).getBoardCards().get(0).getId(), successBoardCard.getId());
        assertEquals(boardDetailsDto.get(0).getBoardCards().get(0).getCardText(), successBoardCard.getText());
        assertEquals(boardDetailsDto.get(0).getBoardCards().get(0).getBoardCardCreator(),
                successBoardCard.getCreator().getEmail());
        assertEquals(boardDetailsDto.get(0).getBoardCards().get(0).getActionTexts().get(0),
                successBoardCard.getBoardCardActions().get(0).getText());

        assertEquals(boardDetailsDto.get(1).getId(), BoardCardsColumn.FAILURES.orderNumber);
        assertEquals(boardDetailsDto.get(1).getBoardCards().get(0).getId(), failureBoardCard.getId());
        assertEquals(boardDetailsDto.get(1).getBoardCards().get(0).getCardText(), failureBoardCard.getText());
        assertEquals(boardDetailsDto.get(1).getBoardCards().get(0).getBoardCardCreator(),
                failureBoardCard.getCreator().getEmail());
        assertEquals(boardDetailsDto.get(1).getBoardCards().get(0).getActionTexts().get(0),
                failureBoardCard.getBoardCardActions().get(0).getText());

        assertEquals(boardDetailsDto.get(2).getId(), BoardCardsColumn.KUDOS.orderNumber);
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
        final User user = new User("123", userEmail, displayName, Set.of());
        final User assignedUser = new User("1234", assignedUserEmail, displayName, Set.of());
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
        assertTrue(boardDetailsDto.toString().contains(String.valueOf(BoardCardsColumn.SUCCESS.orderNumber)));
        assertTrue(boardDetailsDto.toString().contains(successBoardCard.getId().toString()));
        assertTrue(boardDetailsDto.toString().contains(successBoardCard.getText()));
        assertTrue(boardDetailsDto.toString().contains(successBoardCard.getCreator().getEmail()));
        assertTrue(boardDetailsDto.toString().contains(successBoardCard.getBoardCardActions().get(0).getText()));

        assertTrue(boardDetailsDto.toString().contains(assignedUserCard.getId().toString()));
        assertTrue(boardDetailsDto.toString().contains(assignedUserCard.getText()));
        assertTrue(boardDetailsDto.toString().contains(assignedUserCard.getCreator().getEmail()));

        assertTrue(boardDetailsDto.toString().contains(String.valueOf(BoardCardsColumn.FAILURES.orderNumber)));
        assertTrue(boardDetailsDto.toString().contains(failureBoardCard.getId().toString()));
        assertTrue(boardDetailsDto.toString().contains(failureBoardCard.getText()));
        assertTrue(boardDetailsDto.toString().contains(failureBoardCard.getCreator().getEmail()));
        assertTrue(boardDetailsDto.toString().contains(failureBoardCard.getBoardCardActions().get(0).getText()));

        assertTrue(boardDetailsDto.toString().contains(String.valueOf(BoardCardsColumn.KUDOS.orderNumber)));
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
    @DisplayName("assignUsersToBoard should return List of unsuccessfully emails ")
    void assignUsersToBoardShouldReturnFailedEmails() throws JSONException {
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
        final JSONArray failedEmails = new JSONArray(boardService.assignUsersToBoard(boardId, usersEmails, ownerEmail));

        //then
        verify(boardRepository).save(any(Board.class));
        assertEquals(failedEmails.get(0), usersEmails.get(1));
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
