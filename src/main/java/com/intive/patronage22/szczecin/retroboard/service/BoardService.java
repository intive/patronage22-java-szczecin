package com.intive.patronage22.szczecin.retroboard.service;

import com.intive.patronage22.szczecin.retroboard.dto.BoardDataDto;
import com.intive.patronage22.szczecin.retroboard.dto.BoardDto;
import com.intive.patronage22.szczecin.retroboard.dto.EnumStateDto;
import com.intive.patronage22.szczecin.retroboard.exception.UserNotFoundException;
import com.intive.patronage22.szczecin.retroboard.model.Board;
import com.intive.patronage22.szczecin.retroboard.model.User;
import com.intive.patronage22.szczecin.retroboard.repository.BoardRepository;
import com.intive.patronage22.szczecin.retroboard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<BoardDto> getUserBoards(final String uid) {
        if (uid == null || uid.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid request - no uid given!");
        }

        final User user = userRepository.findById(uid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No such user!"));

        return user.getUserBoards().stream().map(BoardDto::fromModel).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BoardDataDto getBoardDataById(final Integer boardId, final String name) {
        userRepository.findUserByName(name)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        boardRepository.findById(boardId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Board not found."));

        // check whether user is assigned to board - if not -> 400
        /*boardRepository.findByIdAndCreatorNameOrByUsersName(boardId, name).ifPresentOrElse(board -> {

        }, () -> {throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is not owner of board.");});
        return BoardDataDto.fromModel();
*/
        /*
        GET

/boards/{id}
Should return all data related to the board by id. (Cards, Votes, Assigned users etc).
Required validation if board exist & if user has access to it (if is owner or assigned to BoardUsers table)


         */
        return BoardDataDto.fromModel();
    }

    @Transactional
    public BoardDto createNewBoard(final String boardName, final String uid) {
        final User user = userRepository.findById(uid).orElseThrow(UserNotFoundException::new);

        final Board newBoard =
                Board.builder().name(boardName).state(EnumStateDto.CREATED).creator(user).users(Set.of()).build();

        return BoardDto.fromModel(boardRepository.save(newBoard));
    }

}
