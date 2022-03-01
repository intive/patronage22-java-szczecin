package com.intive.patronage22.szczecin.retroboard.service;

import com.intive.patronage22.szczecin.retroboard.dto.*;
import com.intive.patronage22.szczecin.retroboard.exception.BadRequestException;
import com.intive.patronage22.szczecin.retroboard.exception.NotFoundException;
import com.intive.patronage22.szczecin.retroboard.model.Board;
import com.intive.patronage22.szczecin.retroboard.model.BoardCard;
import com.intive.patronage22.szczecin.retroboard.model.User;
import com.intive.patronage22.szczecin.retroboard.repository.BoardCardsRepository;
import com.intive.patronage22.szczecin.retroboard.repository.BoardRepository;
import com.intive.patronage22.szczecin.retroboard.repository.UserRepository;
import com.intive.patronage22.szczecin.retroboard.validation.BoardValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final BoardCardsRepository boardCardsRepository;
    private final BoardValidator boardValidator;

    @Transactional(readOnly = true)
    public BoardDataDto getBoardDataById(final Integer boardId, final String name) {
        final User user = userRepository.findUserByName(name)
                .orElseThrow(() -> new BadRequestException("User not found"));

        boardRepository.findById(boardId)
                .orElseThrow(() -> new NotFoundException("Board not found"));

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
                .maximumNumberOfVotes(0)
                .users(Set.of()).build();

        return BoardDto.fromModel(boardRepository.save(newBoard));
    }

    @Transactional
    public BoardDto patchBoard(final Integer id, final BoardPatchDto boardPatchDto, final String uid) {
        boardValidator.validateBoardParameters(boardPatchDto);
        final Board boardReturn;
        final Optional<Board> board = boardRepository.findById(id);

        board.map(b -> Optional.ofNullable(b.getCreator()).filter(creator -> creator.getUid().equals(uid))
                .orElseThrow(() -> new BadRequestException("Not a board owner!")));

        boardReturn = board.map(b -> {
            if (nonNull(boardPatchDto.getName()) && !boardPatchDto.getName().equals(b.getName())) {
                b.setName(boardPatchDto.getName());

            }

            if (nonNull(boardPatchDto.getMaximumNumberOfVotes()) && !boardPatchDto
                    .getMaximumNumberOfVotes().equals(b.getMaximumNumberOfVotes())) {
                b.setMaximumNumberOfVotes(boardPatchDto.getMaximumNumberOfVotes());
            }
            boardRepository.save(b);
            return b;
        }).orElseThrow(() -> new NotFoundException("Board not found!"));

        return BoardDto.fromModel(boardReturn);
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

        final Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new NotFoundException("Board not found"));

        if(!(uid.equals(board.getCreator().getUid()))){
            throw new BadRequestException("User is not owner");
        }else{
            boardRepository.deleteById(boardId);
        }
    }
}
