package com.intive.patronage22.szczecin.retroboard.service;

import com.intive.patronage22.szczecin.retroboard.dto.BoardCardDto;
import com.intive.patronage22.szczecin.retroboard.dto.BoardCardsColumn;
import com.intive.patronage22.szczecin.retroboard.dto.BoardCardsColumnDto;
import com.intive.patronage22.szczecin.retroboard.dto.BoardDataDto;
import com.intive.patronage22.szczecin.retroboard.dto.BoardDetailsDto;
import com.intive.patronage22.szczecin.retroboard.dto.BoardDto;
import com.intive.patronage22.szczecin.retroboard.dto.BoardPatchDto;
import com.intive.patronage22.szczecin.retroboard.dto.EnumStateDto;
import com.intive.patronage22.szczecin.retroboard.dto.UserDto;
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
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        final User user =
                userRepository.findUserByEmail(email).orElseThrow(() -> new BadRequestException("User not found"));

        boardRepository.findById(boardId).orElseThrow(() -> new NotFoundException("Board not found"));

        final Board board = boardRepository.findBoardByIdAndCreatorOrAssignedUser(boardId, user)
                .orElseThrow(() -> new BadRequestException("User has no access to board"));

        final List<BoardCardsColumnDto> boardCardsColumnDtos =
                List.of(BoardCardsColumnDto.createFrom(BoardCardsColumn.SUCCESS),
                        BoardCardsColumnDto.createFrom(BoardCardsColumn.FAILURES),
                        BoardCardsColumnDto.createFrom(BoardCardsColumn.KUDOS));

        final List<UserDto> assignedUsersDtoList = new ArrayList<>();
        board.getUsers().forEach(user1 -> assignedUsersDtoList.add(UserDto.createFrom(user1)));
        assignedUsersDtoList.add(UserDto.createFrom(user));

        return BoardDataDto.createFrom(BoardDto.fromModel(board), boardCardsColumnDtos, assignedUsersDtoList);
    }

    @Transactional(readOnly = true)
    public List<BoardDetailsDto> getBoardDetailsById(final Integer boardId, final String email) {
        final User user =
                userRepository.findUserByEmail(email).orElseThrow(() -> new BadRequestException("User not found"));

        boardRepository.findById(boardId).orElseThrow(() -> new NotFoundException("Board not found"));

        final Board board = boardRepository.findBoardByIdAndCreatorOrAssignedUser(boardId, user)
                .orElseThrow(() -> new BadRequestException("User has no access to board"));

        final List<BoardCard> boardCards = board.getState().equals(EnumStateDto.CREATED) ?
                boardCardsRepository.findAllByBoardIdAndCreatorOrderByIdAsc(boardId, user) :
                boardCardsRepository.findAllByBoardIdOrderByIdAsc(boardId);

        final List<BoardCardDto> successBoardCardsDtos = new ArrayList<>();
        final List<BoardCardDto> failuresBoardCardsDtos = new ArrayList<>();
        final List<BoardCardDto> kudosBoardCardsDtos = new ArrayList<>();

        boardCards.forEach(boardCard -> {
            if (boardCard.getColumn().equals(BoardCardsColumn.SUCCESS)) {
                successBoardCardsDtos.add(BoardCardDto.createFrom(boardCard));
            } else if (boardCard.getColumn().equals(BoardCardsColumn.FAILURES)) {
                failuresBoardCardsDtos.add(BoardCardDto.createFrom(boardCard));
            } else {
                kudosBoardCardsDtos.add(BoardCardDto.createFrom(boardCard));
            }
        });

        return List.of(BoardDetailsDto.createFrom(BoardCardsColumn.SUCCESS.getOrderNumber(), successBoardCardsDtos),
                BoardDetailsDto.createFrom(BoardCardsColumn.FAILURES.getOrderNumber(), failuresBoardCardsDtos),
                BoardDetailsDto.createFrom(BoardCardsColumn.KUDOS.getOrderNumber(), kudosBoardCardsDtos));
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
        final List<BoardDto> assignedBoards = user
                .getUserBoards().stream().map(BoardDto::fromModel).collect(Collectors.toList());
        final List<BoardDto> createdBoards = user
                .getCreatedBoards().stream().map(BoardDto::fromModel).collect(Collectors.toList());

        return Stream.concat(createdBoards.stream(), assignedBoards.stream())
                .distinct()
                .collect(Collectors.toList());
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

        board.getUsers().addAll(usersToAssign);
        boardRepository.save(board);

        return failedEmails;
    }

    @Transactional
    public void removeUserAssignedToTheBoard(final String uid, final Integer boardId, final String email) {

        final User user = userRepository.findById(uid)
                .orElseThrow(() -> new NotFoundException("User is not found"));
        final Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new NotFoundException("Board is not found"));

        if (board.getCreator().equals(user)) {
            throw new BadRequestException("User is the board owner.");
        }
        if (email.equals(user.getEmail()) || email.equals(board.getCreator().getEmail())) {
            board.getUsers().remove(user);
        } else {
            throw new BadRequestException
                    ("Currently logged user is not board owner or user tries to delete other user");
        }
    }
}
