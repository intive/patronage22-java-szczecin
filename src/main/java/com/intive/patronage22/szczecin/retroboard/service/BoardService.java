package com.intive.patronage22.szczecin.retroboard.service;

import com.intive.patronage22.szczecin.retroboard.dto.BoardCardDto;
import com.intive.patronage22.szczecin.retroboard.dto.BoardDataDto;
import com.intive.patronage22.szczecin.retroboard.dto.BoardDto;
import com.intive.patronage22.szczecin.retroboard.dto.EnumStateDto;
import com.intive.patronage22.szczecin.retroboard.exception.BadRequestException;
import com.intive.patronage22.szczecin.retroboard.exception.NotFoundException;
import com.intive.patronage22.szczecin.retroboard.model.Board;
import com.intive.patronage22.szczecin.retroboard.model.BoardCard;
import com.intive.patronage22.szczecin.retroboard.model.User;
import com.intive.patronage22.szczecin.retroboard.repository.BoardCardsRepository;
import com.intive.patronage22.szczecin.retroboard.repository.BoardRepository;
import com.intive.patronage22.szczecin.retroboard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final BoardCardsRepository boardCardsRepository;

    @Transactional(readOnly = true)
    public BoardDataDto getBoardDataById(final Integer boardId, final String name) {
        final User user =
                userRepository.findUserByName(name).orElseThrow(() -> new BadRequestException("User not found"));
        boardRepository.findById(boardId).orElseThrow(() -> new NotFoundException("Board not found"));
        final Board board = boardRepository.findBoardByIdAndCreatorOrAssignedUser(boardId, user)
                .orElseThrow(() -> new BadRequestException("User doesn't have permissions to view board data"));
        final List<BoardCard> boardCards = boardCardsRepository.findAllByBoardId(board.getId());
        final List<BoardCardDto> boardCardDataDtos = new ArrayList<>();
        boardCards.forEach(boardCard -> boardCardDataDtos.add(BoardCardDto.createFrom(boardCard)));
        return BoardDataDto.createFrom(BoardDto.fromModel(board), boardCardDataDtos);
    }

    @Transactional
    public BoardDto createBoard(final String boardName, final String uid) {
        final User user = userRepository.findById(uid).orElseThrow(() -> new NotFoundException("User not found"));

        final Board newBoard = Board.builder()
                .name(boardName)
                .state(EnumStateDto.CREATED)
                .creator(user)
                .users(Set.of()).build();

        return BoardDto.fromModel(boardRepository.save(newBoard));
    }

    @Transactional(readOnly = true)
    public List<BoardDto> getUserBoards(final String uid) {
        if (uid == null || uid.isBlank()) {
            throw new BadRequestException("Invalid request - no uid given!");
        }

        final User user = userRepository.findById(uid).orElseThrow(() -> new BadRequestException("No such user!"));

        return user.getUserBoards().stream().map(BoardDto::fromModel).collect(Collectors.toList());
    }

    @Transactional
    public void delete(final int boardId, final String uid) {

        final User user = userRepository.findById(uid)
                .orElseThrow(() -> new BadRequestException("No such user!"));
        final Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new NotFoundException("Board not found"));

        if(!(uid.equals(board.getCreator().getUid()))){
            throw new BadRequestException("User is not owner");
        }else{
            boardRepository.deleteById(boardId);
        }
    }
}
