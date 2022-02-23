package com.intive.patronage22.szczecin.retroboard.service;

import com.intive.patronage22.szczecin.retroboard.dto.BoardCardsColumn;
import com.intive.patronage22.szczecin.retroboard.dto.BoardDataDto;
import com.intive.patronage22.szczecin.retroboard.dto.BoardDto;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

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
    BoardCardsRepository boardCardsRepository;

    @MockBean
    UserService userService;

    @Test
    void getUserBoardsShouldReturnOk(){
        //given
        final String uid = "1234";
        final User user = new User(uid, "John", Set.of());
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
        when(userRepository.findById(uid)).thenReturn(Optional.of(user));
        final List<BoardDto> boards = boardService.getUserBoards(uid);

        //then
        assertEquals(boards.get(0).getId(), board.getId());
        assertEquals(boards.get(0).getName(), board.getName());
        assertEquals(boards.get(0).getState(), board.getState());
    }

    @Test
    void getUserBoardsShouldThrowBadRequestWhenUidIsBlank(){
        //given
        final String uid = "";

        //when

        //then
        assertThrows(BadRequestException.class, () -> boardService.getUserBoards(uid));
    }

    @Test
    void getUserBoardsShouldThrowBadRequestWhenUidIsNull(){
        //given
        final String uid = null;

        //when

        //then
        assertThrows(BadRequestException.class, () -> boardService.getUserBoards(uid));
    }

    @Test
    void getUserBoardsShouldThrowBadRequestWhenUserDoesNotExist(){
        //given
        final String uid = "123";

        //when
        when(userRepository.findById(uid)).thenThrow(BadRequestException.class);

        //then
        assertThrows(BadRequestException.class, () -> boardService.getUserBoards(uid));
    }

    @Test
    void createBoardShouldReturnBoardDtoWhenUserExistsAndBoardNameIsValid() {
        // given
        final String uid = "uid101";
        final String boardName = "My first board.";

        final User user = new User(uid, "Josef", Set.of());

        final Board board = Board.builder()
                .id(10)
                .name(boardName)
                .state(EnumStateDto.CREATED)
                .creator(user)
                .users(Set.of())
                .build();

        // when
        when(userRepository.findById(uid)).thenReturn(Optional.of(user));
        when(boardRepository.save(any(Board.class))).thenReturn(board);
        final BoardDto boardDtoResult = boardService.createBoard(boardName, uid);

        // then
        assertEquals(BoardDto.fromModel(board), boardDtoResult);
        verify(userRepository).findById(uid);
        verify(boardRepository).save(any(Board.class));
    }

    @Test
    void createBoardShouldReturnNotFoundWhenUserDoesNotExist() {
        // given
        final String uid = "uid101";
        final String boardName = "My first board.";

        // when
        when(userRepository.findById(uid)).thenReturn(Optional.empty());

        // then
        assertThrows(NotFoundException.class, () -> boardService.createBoard(boardName, uid));
    }

    @Test
    @DisplayName("getBoardDataById should throw 400 when user does not exist")
    void getBoardDataByIdShouldThrowBadRequestWhenUserDoesNotExist() {
        //given
        final int boardId = 1;
        final String username = "testemail@example.com";

        //when
        when(userRepository.findUserByName(username)).thenReturn(Optional.empty());

        //then
        assertThrows(BadRequestException.class, () -> boardService.getBoardDataById(boardId, username));
    }

    @Test
    @DisplayName("getBoardDataById should throw 404 when board does not exist")
    void getBoardDataByIdShouldThrowNotFoundWhenBoardDoesNotExist() {
        //given
        final int boardId = 1;
        final String username = "testemail@example.com";
        final User user = new User("123", username, Set.of());

        //when
        when(userRepository.findUserByName(username)).thenReturn(Optional.of(user));
        when(boardRepository.findById(boardId)).thenReturn(Optional.empty());

        //then
        assertThrows(NotFoundException.class, () -> boardService.getBoardDataById(boardId, username));
    }

    @Test
    @DisplayName("getBoardDataById should throw 400 when user has no permission to view board")
    void getBoardDataByIdShouldThrowBadRequestWhenUserDoesntHavePermissions() {
        //given
        final int boardId = 1;
        final String username = "testemail@example.com";
        final User user = new User("123", username, Set.of());
        final User assignUser = new User("1234", "assignUser", Set.of());
        final Board board = Board.builder()
                .id(1)
                .name("board name")
                .state(EnumStateDto.CREATED)
                .creator(user)
                .users(Set.of(assignUser))
                .boardCards(Set.of()).build();

        //when
        when(userRepository.findUserByName(username)).thenReturn(Optional.of(user));
        when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
        when(boardRepository.findBoardByIdAndCreatorOrAssignedUser(boardId, user)).thenReturn(Optional.empty());

        //then
        assertThrows(BadRequestException.class, () -> boardService.getBoardDataById(boardId, username));
    }

    @Test
    @DisplayName("getBoardDataById should return 200")
    void getBoardDataByIdShouldReturnOk() {
        //given
        final int boardId = 1;
        final String username = "testemail@example.com";
        final User user = new User("123", username, Set.of());
        final User assignUser = new User("1234", "assignUser", Set.of());
        final Board board = Board.builder()
                .id(boardId)
                .name("board name")
                .state(EnumStateDto.CREATED)
                .creator(user)
                .users(Set.of(assignUser))
                .boardCards(Set.of()).build();
        final BoardCard boardCard = new BoardCard(2, board, "test card name", BoardCardsColumn.SUCCESS, user, List.of());
        final BoardCardAction boardCardAction = new BoardCardAction(4, boardCard, "test action");
        board.setBoardCards(Set.of(boardCard));
        boardCard.setBoardCardActions(List.of(boardCardAction));
        final List<BoardCard> boardCards = List.of(boardCard);

        //when
        when(userRepository.findUserByName(username)).thenReturn(Optional.of(user));
        when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
        when(boardRepository.findBoardByIdAndCreatorOrAssignedUser(boardId, user)).thenReturn(Optional.of(board));
        when(boardCardsRepository.findAllByBoardId(boardId)).thenReturn(boardCards);

        final BoardDataDto boardDataDto = boardService.getBoardDataById(boardId, username);

        //then
        assertEquals(boardDataDto.getBoard().getId(), board.getId());
        assertEquals(boardDataDto.getBoard().getName(), board.getName());
        assertEquals(boardDataDto.getBoard().getState(), board.getState());
        assertEquals(boardDataDto.getBoardCards().get(0).getId(), boardCards.get(0).getId());
        assertEquals(boardDataDto.getBoardCards().get(0).getCardText(), boardCards.get(0).getText());
        assertEquals(boardDataDto.getBoardCards().get(0).getColumnName(), boardCards.get(0).getColumn());
        assertEquals(boardDataDto.getBoardCards().get(0).getBoardCardCreator(),
                boardCards.get(0).getCreator().getName());
        assertEquals(boardDataDto.getBoardCards().get(0).getActionTexts(),
                List.of(boardCards.get(0).getBoardCardActions().get(0).getText()));
    }

    @Test
    void deleteBoardShouldReturnNotFoundWhenUserNotExists(){

        //given
        final String uid = "uid101";
        final int boardId = 101;

        //when
        when(userRepository.findById(uid)).thenReturn(Optional.empty());

        //then
        assertThrows(NotFoundException.class,
                () -> boardService.delete(boardId,uid));
    }

    @Test
    void deleteBoardShouldReturnNotFoundWhenBoardNotExist(){

        //given
        final String uid = "uid101";
        final int boardId = 101;

        //when
        when(boardRepository.findById(boardId)).thenReturn(Optional.empty());

        //then
        assertThrows(NotFoundException.class,
                () -> boardService.delete(boardId,uid));
    }
}
