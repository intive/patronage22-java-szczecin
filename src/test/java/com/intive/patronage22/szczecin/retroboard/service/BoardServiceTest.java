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
import com.intive.patronage22.szczecin.retroboard.model.BoardCardVotes;
import com.intive.patronage22.szczecin.retroboard.model.BoardCardVotesKey;
import com.intive.patronage22.szczecin.retroboard.model.User;
import com.intive.patronage22.szczecin.retroboard.repository.BoardCardsRepository;
import com.intive.patronage22.szczecin.retroboard.repository.BoardCardsVotesRepository;
import com.intive.patronage22.szczecin.retroboard.repository.BoardRepository;
import com.intive.patronage22.szczecin.retroboard.repository.UserRepository;
import com.intive.patronage22.szczecin.retroboard.validation.BoardValidator;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

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

    @MockBean
    BoardCardsVotesRepository boardCardsVotesRepository;

    @Test
    void getUserBoardsShouldReturnSortedListByIdWhenUserExists() {
        //given
        final String uid = "1234";
        final String email = "John@test.pl";
        final User user = new User(uid, email, "john14", false, Set.of(), Set.of());
        final var board = TestUtils.buildBoard(10, EnumStateDto.CREATED, user, Set.of(), 0);
        final var board1 = TestUtils.buildBoard(20, EnumStateDto.CREATED, user, Set.of(), 0);
        user.setUserBoards(Set.of(board, board1));

        //when
        when(userRepository.findUserByEmail(email)).thenReturn(Optional.of(user));
        final List<BoardDto> boards = boardService.getUserBoards(email);

        //then
        assertEquals(boards.get(0).getId(), board1.getId());
        assertEquals(boards.get(0).getName(), board1.getName());
        assertEquals(boards.get(0).getState(), board1.getState());

        assertEquals(boards.get(1).getId(), board.getId());
        assertEquals(boards.get(1).getName(), board.getName());
        assertEquals(boards.get(1).getState(), board.getState());
    }

    @Test
    void getUserBoardsShouldThrowBadRequestWhenEmailIsBlank() {
        //given
        final String email = "";

        //then
        assertThrows(BadRequestException.class, () -> boardService.getUserBoards(email));
    }

    @Test
    void getUserBoardsShouldThrowBadRequestWhenEmailIsNull() {
        //given
        final String email = null;

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

        final User user = new User(uid, email, "josef14", false, Set.of(), Set.of());

        final Board board = TestUtils.buildBoard(10, EnumStateDto.CREATED, user, Set.of(), 5);

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
        final User user = new User("123", email, displayName, false, Set.of(), Set.of());

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
        final User user = new User("123", email, displayName, false, Set.of(), Set.of());
        final User assignUser = new User("1234", "assignUser@test.pl", "test1", false, Set.of(), Set.of());
        final Board board = TestUtils.buildBoard(boardId, EnumStateDto.CREATED, user, Set.of(assignUser), 5);

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
        final User user = new User("123", email, displayName, false, Set.of(), Set.of());
        final User assignUser = new User("1234", "assignUser", "test1", false, Set.of(), Set.of());
        final Board board = TestUtils.buildBoard(boardId, EnumStateDto.CREATED, user, Set.of(assignUser), 5);
        final BoardCard boardCard =new BoardCard
                (2, board, "test card name", BoardCardsColumn.SUCCESS, user, List.of());
        final BoardCardAction boardCardAction = new BoardCardAction(4, boardCard, "test action");
        board.setBoardCards(Set.of(boardCard));
        boardCard.setBoardCardActions(List.of(boardCardAction));

        //when
        when(userRepository.findUserByEmail(email)).thenReturn(Optional.of(user));
        when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
        when(boardRepository.findBoardByIdAndCreatorOrAssignedUser(boardId, user)).thenReturn(Optional.of(board));

        final BoardDataDto boardDataDto = boardService.getBoardDataById(boardId, email);

        //then
        assertEquals(board.getId(), boardDataDto.getBoard().getId());
        assertEquals(board.getState(), boardDataDto.getBoard().getState());
        assertEquals(board.getName(), boardDataDto.getBoard().getName());
        assertEquals(board.getMaximumNumberOfVotes(), boardDataDto.getBoard().getNumberOfVotes());
        assertEquals(BoardCardsColumn.SUCCESS.name(), boardDataDto.getColumns().get(0).getName());
        assertEquals(BoardCardsColumn.SUCCESS.getColumnId(), boardDataDto.getColumns().get(0).getId());
        assertEquals(BoardCardsColumn.SUCCESS.getColumnId(), boardDataDto.getColumns().get(0).getPosition());
        assertEquals(BoardCardsColumn.SUCCESS.getColour(), boardDataDto.getColumns().get(0).getColour());
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
        final User user = new User("123", email, displayName, false, Set.of(), Set.of());

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
        final User user = new User("123", email, displayName, false, Set.of(), Set.of());
        final User assignUser = new User("1234", "assignUser@test.pl", "test1", false, Set.of(), Set.of());
        final Board board = TestUtils.buildBoard(boardId, EnumStateDto.CREATED, user, Set.of(assignUser), 5);

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
        final int numberOfUserVotes = 2;

        final User user = new User("123", userEmail, displayName, false, Set.of(), Set.of());
        final User assignedUser = new User("1234", assignedUserEmail, displayName, false, Set.of(), Set.of());
        final Board board = TestUtils.buildBoard(boardId, EnumStateDto.CREATED, user, Set.of(assignedUser), 5);

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

        final BoardCardVotes successBoardCardVotes =
                new BoardCardVotes(new BoardCardVotesKey(successBoardCard.getId(), user.getUid()), successBoardCard,
                        user, numberOfUserVotes);
        final BoardCardVotes assignedUserSuccessCardVotes =
                new BoardCardVotes(new BoardCardVotesKey(successBoardCard.getId(), assignedUser.getUid()),
                        successBoardCard, assignedUser, numberOfUserVotes);

        //when
        when(userRepository.findUserByEmail(userEmail)).thenReturn(Optional.of(user));
        when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
        when(boardRepository.findBoardByIdAndCreatorOrAssignedUser(boardId, user)).thenReturn(Optional.of(board));
        when(boardCardsRepository.findAllByBoardIdOrderByIdAsc(boardId)).thenReturn(
                List.of(successBoardCard, failureBoardCard, kudosBoardCard));
        when(boardCardsVotesRepository.getVotesByBoardAndCard(board, successBoardCard)).thenReturn(
                Optional.of(successBoardCardVotes.getVotes() + assignedUserSuccessCardVotes.getVotes()));
        when(boardCardsVotesRepository.getVotesByBoardAndCardAndUser(board, successBoardCard, user)).thenReturn(
                Optional.of(successBoardCardVotes.getVotes()));

        final List<BoardDetailsDto> boardDetailsDto = boardService.getBoardDetailsById(boardId, userEmail);

        //then
        assertEquals(BoardCardsColumn.SUCCESS.getColumnId(), boardDetailsDto.get(0).getId());
        assertEquals(successBoardCard.getId(), boardDetailsDto.get(0).getBoardCards().get(0).getId());
        assertEquals(successBoardCard.getText(), boardDetailsDto.get(0).getBoardCards().get(0).getCardText());
        assertEquals(successBoardCard.getCreator().getEmail(),
                boardDetailsDto.get(0).getBoardCards().get(0).getBoardCardCreator());
        assertEquals(successBoardCard.getBoardCardActions().get(0).getText(),
                boardDetailsDto.get(0).getBoardCards().get(0).getActionTexts().get(0));
        assertEquals(successBoardCardVotes.getVotes() + assignedUserSuccessCardVotes.getVotes(),
                boardDetailsDto.get(0).getBoardCards().get(0).getVotes());
        assertEquals(successBoardCardVotes.getVotes(), boardDetailsDto.get(0).getBoardCards().get(0).getUserVotes());

        assertEquals(BoardCardsColumn.FAILURES.getColumnId(), boardDetailsDto.get(1).getId());
        assertEquals(failureBoardCard.getId(), boardDetailsDto.get(1).getBoardCards().get(0).getId());
        assertEquals(failureBoardCard.getText(), boardDetailsDto.get(1).getBoardCards().get(0).getCardText());
        assertEquals(failureBoardCard.getCreator().getEmail(),
                boardDetailsDto.get(1).getBoardCards().get(0).getBoardCardCreator());
        assertEquals(failureBoardCard.getBoardCardActions().get(0).getText(),
                boardDetailsDto.get(1).getBoardCards().get(0).getActionTexts().get(0));
        assertEquals(0, boardDetailsDto.get(1).getBoardCards().get(0).getVotes());
        assertEquals(0, boardDetailsDto.get(1).getBoardCards().get(0).getUserVotes());

        assertEquals(BoardCardsColumn.KUDOS.getColumnId(), boardDetailsDto.get(2).getId());
        assertEquals(kudosBoardCard.getId(), boardDetailsDto.get(2).getBoardCards().get(0).getId());
        assertEquals(kudosBoardCard.getText(), boardDetailsDto.get(2).getBoardCards().get(0).getCardText());
        assertEquals(kudosBoardCard.getCreator().getEmail(),
                boardDetailsDto.get(2).getBoardCards().get(0).getBoardCardCreator());
        assertEquals(kudosBoardCard.getBoardCardActions().get(0).getText(),
                boardDetailsDto.get(2).getBoardCards().get(0).getActionTexts().get(0));
        assertEquals(0, boardDetailsDto.get(2).getBoardCards().get(0).getVotes());
        assertEquals(0, boardDetailsDto.get(2).getBoardCards().get(0).getUserVotes());

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
        final int numberOfUserVotes = 2;

        final User user = new User("123", userEmail, displayName, false, Set.of(), Set.of());
        final User assignedUser = new User("1234", assignedUserEmail, displayName, false, Set.of(), Set.of());
        final Board board = TestUtils.buildBoard(boardId, EnumStateDto.VOTING, user, Set.of(assignedUser), 5);

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

        final BoardCardVotes failureBoardCardVotes =
                new BoardCardVotes(new BoardCardVotesKey(failureBoardCard.getId(), user.getUid()), failureBoardCard,
                        user, numberOfUserVotes);

        //when
        when(userRepository.findUserByEmail(userEmail)).thenReturn(Optional.of(user));
        when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
        when(boardRepository.findBoardByIdAndCreatorOrAssignedUser(boardId, user)).thenReturn(Optional.of(board));
        when(boardCardsRepository.findAllByBoardIdOrderByIdAsc(boardId)).thenReturn(
                List.of(successBoardCard, assignedUserCard, failureBoardCard, kudosBoardCard));
        when(boardCardsVotesRepository.getVotesByBoardAndCard(board, failureBoardCard)).thenReturn(
                Optional.of(failureBoardCardVotes.getVotes()));
        when(boardCardsVotesRepository.getVotesByBoardAndCardAndUser(board, failureBoardCard, user)).thenReturn(
                Optional.of(failureBoardCardVotes.getVotes()));

        final List<BoardDetailsDto> boardDetailsDto = boardService.getBoardDetailsById(boardId, userEmail);

        //then
        assertEquals(BoardCardsColumn.SUCCESS.getColumnId(), boardDetailsDto.get(0).getId());
        assertEquals(successBoardCard.getId(), boardDetailsDto.get(0).getBoardCards().get(0).getId());
        assertEquals(successBoardCard.getText(), boardDetailsDto.get(0).getBoardCards().get(0).getCardText());
        assertEquals(successBoardCard.getCreator().getEmail(),
                boardDetailsDto.get(0).getBoardCards().get(0).getBoardCardCreator());
        assertEquals(successBoardCard.getBoardCardActions().get(0).getText(),
                boardDetailsDto.get(0).getBoardCards().get(0).getActionTexts().get(0));
        assertEquals(0, boardDetailsDto.get(0).getBoardCards().get(0).getVotes());
        assertEquals(0, boardDetailsDto.get(0).getBoardCards().get(0).getUserVotes());

        assertEquals(assignedUserCard.getId(), boardDetailsDto.get(0).getBoardCards().get(1).getId());
        assertEquals(assignedUserCard.getText(), boardDetailsDto.get(0).getBoardCards().get(1).getCardText());
        assertEquals(assignedUserCard.getCreator().getEmail(),
                boardDetailsDto.get(0).getBoardCards().get(1).getBoardCardCreator());
        assertEquals(assignedUserCard.getBoardCardActions(),
                boardDetailsDto.get(0).getBoardCards().get(1).getActionTexts());
        assertEquals(0, boardDetailsDto.get(0).getBoardCards().get(1).getVotes());
        assertEquals(0, boardDetailsDto.get(0).getBoardCards().get(1).getUserVotes());

        assertEquals(BoardCardsColumn.FAILURES.getColumnId(), boardDetailsDto.get(1).getId());
        assertEquals(failureBoardCard.getId(), boardDetailsDto.get(1).getBoardCards().get(0).getId());
        assertEquals(failureBoardCard.getText(), boardDetailsDto.get(1).getBoardCards().get(0).getCardText());
        assertEquals(failureBoardCard.getCreator().getEmail(),
                boardDetailsDto.get(1).getBoardCards().get(0).getBoardCardCreator());
        assertEquals(failureBoardCard.getBoardCardActions().get(0).getText(),
                boardDetailsDto.get(1).getBoardCards().get(0).getActionTexts().get(0));
        assertEquals(failureBoardCardVotes.getVotes(), boardDetailsDto.get(1).getBoardCards().get(0).getVotes());
        assertEquals(failureBoardCardVotes.getVotes(), boardDetailsDto.get(1).getBoardCards().get(0).getUserVotes());

        assertEquals(BoardCardsColumn.KUDOS.getColumnId(), boardDetailsDto.get(2).getId());
        assertEquals(kudosBoardCard.getId(), boardDetailsDto.get(2).getBoardCards().get(0).getId());
        assertEquals(kudosBoardCard.getText(), boardDetailsDto.get(2).getBoardCards().get(0).getCardText());
        assertEquals(kudosBoardCard.getCreator().getEmail(),
                boardDetailsDto.get(2).getBoardCards().get(0).getBoardCardCreator());
        assertEquals(kudosBoardCard.getBoardCardActions().get(0).getText(),
                boardDetailsDto.get(2).getBoardCards().get(0).getActionTexts().get(0));
        assertEquals(0, boardDetailsDto.get(2).getBoardCards().get(0).getVotes());
        assertEquals(0, boardDetailsDto.get(2).getBoardCards().get(0).getUserVotes());
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
        final User userOwner = new User(uidOwner, emailOwner, "displayName", false, Set.of(), Set.of());
        final User user = new User(uid, email, "displayName", false, Set.of(), Set.of());
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
        final User userOwner = new User(uidOwner, emailOwner, "displayName", false, Set.of(), Set.of());
        final User user = new User(uid, email, "displayName", false, Set.of(), Set.of());
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
        final var user = new User(uid, email, "displayName", false, Set.of(), Set.of());
        final var board = TestUtils.buildBoard(10, EnumStateDto.CREATED, user, Set.of(), 3);
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
        final var userOwner = new User(uid, email, "displayName", false, Set.of(), Set.of());
        final var board = TestUtils.buildBoard(10, EnumStateDto.CREATED, userOwner, Set.of(), 3);
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
        final var user = new User(uid, email, "displayName", false, Set.of(), Set.of());
        final var board = TestUtils.buildBoard(10, EnumStateDto.CREATED, user, Set.of(), 3);
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
        final User user = new User("123", email, "test name", false, Set.of(), Set.of());

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
        final User user = new User("123", email, "test name", false, Set.of(), Set.of());
        final User boardOwner = new User("1234", "testemail1@example.com", "test name", false, Set.of(), Set.of());

        //when
        when(userRepository.findUserByEmail(email)).thenReturn(Optional.of(user));
        when(boardRepository.findById(boardId))
                .thenReturn(Optional.of(TestUtils.buildBoard(10, EnumStateDto.CREATED, boardOwner, Set.of(), 3)));

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
        final User owner = new User("123", ownerEmail, displayName, false, Set.of(), Set.of());
        final User userToAssign = new User("126", usersEmails.get(0), displayName, false, new HashSet<>(), Set.of());
        final User existingUser = new User("1234", "test@123.pl", displayName, false, new HashSet<>(), Set.of());
        final Set<User> boardUsers = new HashSet<>(List.of(existingUser));
        final List<User> existingUsers = List.of(userToAssign, existingUser);

        final Board board = TestUtils.buildBoard(10, EnumStateDto.CREATED, owner, boardUsers, 3);

        //when
        when(userRepository.findUserByEmail(ownerEmail)).thenReturn(Optional.of(owner));
        when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
        when(userRepository.findAllNotDeactivatedByEmailIn(usersEmails)).thenReturn(existingUsers);
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
        final var user = new User(uid, email, "john14", false, Set.of(), Set.of());
        final var board = TestUtils.buildBoard(10, EnumStateDto.VOTING, user, Set.of(), 3);
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
        final var user = new User(uid, email, "john14", false, Set.of(), Set.of());
        final var board = TestUtils.buildBoard(10, EnumStateDto.CREATED, user, Set.of(), 3);
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
        final var user1 = new User(uid1, email1, "displayName1", false, Set.of(), Set.of());
        final var user2 = new User(uid2, email2, "displayName2", false, Set.of(), Set.of());
        final var boardCreated = TestUtils.buildBoard(10, EnumStateDto.CREATED, user1, Set.of(), 3);
        final var boardAssigned = TestUtils.buildBoard(11, EnumStateDto.CREATED, user2, Collections.singleton(user1), 3);
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

        final User notAssignedUser = new User(uid, "test@test.com", "some_user", false, Set.of(), Set.of());
        final User boardOwner = new User("123", email, "board_owner", false, Set.of(), Set.of());
        final Board board = TestUtils.buildBoard(boardId, EnumStateDto.CREATED, boardOwner, Set.of(), 3);

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

        final User notAssignedUser = new User(uid, email, "some_user", false, Set.of(), Set.of());
        final User boardOwner = new User("123", "boardOwner@test.pl", "board_owner", false, Set.of(), Set.of());
        final Board board = TestUtils.buildBoard(boardId, EnumStateDto.CREATED, boardOwner, Set.of(), 3);

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

        final User boardOwner = new User("123", "boardOwner@test1.com", "board_owner", false, Set.of(), Set.of());
        final User userToRemove = new User(uid, "test3@test3.com", "userTest", false, Set.of(), Set.of());
        final Board board = TestUtils.buildBoard(boardId, EnumStateDto.CREATED, boardOwner, Set.of(userToRemove), 3);

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

        final User boardOwner = new User(uid, email, "board_owner", false, Set.of(), Set.of());
        final Board board = TestUtils.buildBoard(boardId, EnumStateDto.CREATED, boardOwner, Set.of(), 3);

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

        final User boardOwner = new User("12345", email, "board_owner", false, Set.of(), Set.of());
        final User userToRemove = new User(uid, "test2@test2.com", "userTest", false, Set.of(), Set.of());
        final Board board = TestUtils.buildBoard(boardId, EnumStateDto.CREATED, boardOwner, new HashSet<>(), 3);
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

        final User boardOwner = new User("12345", "boardOwner@test1.com", "board_owner", false, Set.of(), Set.of());
        final User currentlyLogged = new User(uid, email, "currently_logged", false, Set.of(), Set.of());
        final Board board = TestUtils.buildBoard(boardId, EnumStateDto.CREATED, boardOwner, new HashSet<>(), 3);
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
    @DisplayName("When setNextState is called - it will change state from CREATED to VOTING (next) and return board details.")
    void setNextStateChangeStateValueAndSaveTheData() {
        // given
        final var uid = "uid101";
        final var email = "username@test.pl";
        final var user = new User(uid, email, "displayName", false, Set.of(), Set.of());
        final var board = TestUtils.buildBoard(10, EnumStateDto.CREATED, user, Set.of(), 3);
        when(boardRepository.save(any(Board.class))).thenReturn(board);
        when(boardRepository.findById(board.getId())).thenReturn(Optional.of(board));
        when(userRepository.findUserByEmail(email)).thenReturn(Optional.of(user));

        // when
        final var boardDtoResult = boardService.setNextState(board.getId(), email);

        // then
        assertEquals(boardDtoResult.getBoard().getState(), EnumStateDto.VOTING);
        verify(boardRepository).findById(board.getId());
        verify(boardRepository).save(any(Board.class));
    }

    @Test
    @DisplayName("When setNextState is used on a board with EnumStateDto set to DONE - Not Acceptable Exception is thrown.")
    void setNextStateThrowsNotAcceptableIfStateIsDone() {
        // given
        final var uid = "uid101";
        final var email = "username@test.pl";
        final var user = new User(uid, email, "displayName", false, Set.of(), Set.of());
        final var board = TestUtils.buildBoard(10, EnumStateDto.DONE, user, Set.of(), 3);
        when(boardRepository.save(any(Board.class))).thenReturn(board);
        when(boardRepository.findById(board.getId())).thenReturn(Optional.of(board));
        when(userRepository.findUserByEmail(email)).thenReturn(Optional.of(user));

        // when
        final NotAcceptableException exception = assertThrows(
                NotAcceptableException.class, () -> boardService.setNextState(board.getId(), email));

        // then
        assertEquals("Already in last state", exception.getMessage());
    }

    @Test
    @DisplayName("When setNextState is used on a board which does not exist - Not Found Exception is thrown.")
    void setNextStateThrowsNotFoundIfBoardIsNotFound() {
        // given
        final var uid = "uid101";
        final var email = "username@test.pl";
        final var user = new User(uid, email, "displayName", false, Set.of(), Set.of());
        final var board_id = 1;
        when(userRepository.findUserByEmail(email)).thenReturn(Optional.of(user));
        when(boardRepository.findById(board_id)).thenReturn(Optional.empty());

        // when
        final NotFoundException exception = assertThrows(
                NotFoundException.class, () -> boardService.setNextState(board_id,email));

        // then
        assertEquals("Board not found", exception.getMessage());
    }

    @Test
    @DisplayName("When setNextState is used on a board which the user is not a creator of - Not Found Exception is thrown.")
    void setNextStateThrowsNotFoundIfUserIsNotAnOwner() {
        // given
        final var uid1 = "uid101";
        final var email1 = "username1@test.pl";
        final var uid2 = "uid102";
        final var email2 = "username2@test.pl";
        final var owner = new User(uid1, email1, "displayName", false, Set.of(), Set.of());
        final var user = new User(uid2, email2, "displayName2", false, Set.of(), Set.of());
        final var board = TestUtils.buildBoard(10, EnumStateDto.CREATED, owner, Set.of(), 3);
        when(boardRepository.findById(board.getId())).thenReturn(Optional.of(board));
        when(userRepository.findUserByEmail(email2)).thenReturn(Optional.of(user));

        // when
        final NotFoundException exception = assertThrows(
                NotFoundException.class, () -> boardService.setNextState(board.getId(), email2));

        // then
        assertEquals("User is not the board owner.", exception.getMessage());
    }

    @Test
    @DisplayName("When setNextState() is called on a board with maximumNumberOfVotes = 0 -> Bad Request Exception is thrown.")
    void setNextStateThrowsBadRequestWhenNumberOfVotesIsNotSet() {
        // given
        final var uid = "uid101";
        final var email = "username@test.pl";
        final var user = new User(uid, email, "displayName", false, Set.of(), Set.of());
        final var board = TestUtils.buildBoard(10, EnumStateDto.CREATED, user, Set.of(), 0);
        when(boardRepository.save(any(Board.class))).thenReturn(board);
        when(boardRepository.findById(board.getId())).thenReturn(Optional.of(board));
        when(userRepository.findUserByEmail(email)).thenReturn(Optional.of(user));

        // when
        final BadRequestException exception = assertThrows(
                BadRequestException.class, () -> boardService.setNextState(board.getId(), email));

        // then
        assertEquals("Number of votes not set!", exception.getMessage());
    }
}