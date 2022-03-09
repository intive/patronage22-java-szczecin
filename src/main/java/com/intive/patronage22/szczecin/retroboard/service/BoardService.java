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

import java.util.*;
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
    public BoardDataDto getBoardDataById(final Integer boardId, final String email) {
        final User user = userRepository.findUserByEmail(email)
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
    public BoardDto createBoard(final String boardName, final String email) {
        final User user = userRepository.findUserByEmail(email)
                .orElseThrow(() -> new BadRequestException("User not found"));

        final Board newBoard = Board.builder()
                .name(boardName)
                .state(EnumStateDto.CREATED)
                .creator(user)
                .maximumNumberOfVotes(0)
                .users(Set.of()).build();

        return BoardDto.fromModel(boardRepository.save(newBoard));
    }

    @Transactional
    public BoardDto patchBoard(final Integer id, final BoardPatchDto boardPatchDto, final String email) {
        boardValidator.validateBoardParameters(boardPatchDto);
        final Board boardReturn;
        final Optional<Board> board = boardRepository.findById(id);

        boardReturn = board.map(b -> {
            if (b.getState() == EnumStateDto.CREATED) {
                Optional.ofNullable(b.getCreator())
                        .filter(creator -> creator.getEmail().equals(email))
                        .orElseThrow(() -> new BadRequestException("Not a board owner!"));
            } else {
                throw new BadRequestException("State of board does not allow to change number of votes!");
            }

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
    public List<BoardDto> getUserBoards(final String email) {
        final User user = userRepository.findUserByEmail(email)
                .orElseThrow(() -> new BadRequestException("User not found"));

        return user.getUserBoards().stream().map(BoardDto::fromModel).collect(Collectors.toList());
    }

    @Transactional
    public void delete(final int boardId, final String email) {

        final Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new NotFoundException("Board not found"));

        if (!(email.equals(board.getCreator().getEmail()))) {
            throw new BadRequestException("User is not owner");
        } else {
            boardRepository.deleteById(boardId);
        }
    }

    @Transactional
    public List<String> assignUsersToBoard(final Integer boardId, final List<String> usersEmails, final String email) {
        final User boardOwner =
                userRepository.findUserByEmail(email).orElseThrow(() -> new NotFoundException("User is not found"));
        final Board board =
                boardRepository.findById(boardId).orElseThrow(() -> new NotFoundException("Board is not found."));
        if (!board.getCreator().equals(boardOwner)) {
            throw new BadRequestException("User is not the board owner.");
        }
        final Set<User> usersToAssign = new HashSet<>();
        final List<String> failedEmails = new ArrayList<>();

        for (final String userEmail : usersEmails) {
            userRepository.findUserByEmail(userEmail)
                    .ifPresentOrElse(usersToAssign::add, () -> failedEmails.add(userEmail));
        }
        board.setUsers(usersToAssign);
        boardRepository.save(board);

        return failedEmails;
    }
}
