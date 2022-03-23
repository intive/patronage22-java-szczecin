package com.intive.patronage22.szczecin.retroboard.service;

import com.intive.patronage22.szczecin.retroboard.dto.BoardCardDto;
import com.intive.patronage22.szczecin.retroboard.dto.BoardCardsColumn;
import com.intive.patronage22.szczecin.retroboard.dto.EnumStateDto;
import com.intive.patronage22.szczecin.retroboard.exception.BadRequestException;
import com.intive.patronage22.szczecin.retroboard.exception.NotFoundException;
import com.intive.patronage22.szczecin.retroboard.model.Board;
import com.intive.patronage22.szczecin.retroboard.model.BoardCard;
import com.intive.patronage22.szczecin.retroboard.model.BoardCardVotes;
import com.intive.patronage22.szczecin.retroboard.model.BoardCardVotesKey;
import com.intive.patronage22.szczecin.retroboard.model.User;
import com.intive.patronage22.szczecin.retroboard.repository.BoardCardsRepository;
import com.intive.patronage22.szczecin.retroboard.repository.BoardCardsVotesRepository;
import com.intive.patronage22.szczecin.retroboard.repository.BoardRepository;
import com.intive.patronage22.szczecin.retroboard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BoardCardService {

    private final BoardCardsRepository boardCardsRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final BoardCardsVotesRepository boardCardsVotesRepository;

    @Transactional
    public BoardCardDto createBoardCard(final BoardCardDto boardCardDto, final Integer boardId, final String email) {

        final User user = userRepository
                .findUserByEmail(email).orElseThrow(() -> new BadRequestException("User not found"));

        if (!boardRepository.existsById(boardId)) {
            throw new NotFoundException("Board not found");
        }

        final Board board = boardRepository.findBoardByIdAndCreatorOrAssignedUser(boardId, user)
                .orElseThrow(() -> new BadRequestException("User has no access to board"));

        if (!board.getState().equals(EnumStateDto.CREATED)) {
            throw new BadRequestException("Board state is not CREATED");
        }

        final BoardCard boardCard = BoardCard.builder()
                .board(board)
                .column(BoardCardsColumn.columnIdToBoardCardsColumn(boardCardDto.getColumnId()))
                .text(boardCardDto.getCardText())
                .creator(user)
                .boardCardActions(List.of())
                .build();

        boardCardsRepository.save(boardCard);

        return BoardCardDto.createFrom(boardCard);
    }

    @Transactional
    public void removeCard(final Integer cardId, final String email) {

        final BoardCard boardCard = boardCardsRepository.findById(cardId)
                .orElseThrow(() -> new NotFoundException("Card not found"));

        final User user = userRepository
                .findUserByEmail(email).orElseThrow(() -> new BadRequestException("User not found"));

        if (!user.equals(boardCard.getCreator())
                && !user.equals(boardCard.getBoard().getCreator()))
            throw new BadRequestException("User is not allowed to delete card");

        if (!EnumStateDto.CREATED.equals(boardCard.getBoard().getState()))
            throw new BadRequestException("User is not allowed to delete card");

        boardCardsRepository.deleteById(cardId);
    }

    @Transactional
    public Map<String, Integer> addVote(final Integer cardId, final String email) {
        final User user =
                userRepository.findUserByEmail(email).orElseThrow(() -> new BadRequestException("User not found"));

        final BoardCard card =
                boardCardsRepository.findById(cardId).orElseThrow(() -> new NotFoundException("Card not found"));

        final Board board = boardRepository.findById(card.getBoard().getId())
                .orElseThrow(() -> new BadRequestException("Board not exist"));

        if (!board.getUsers().contains(user) && !board.getCreator().equals(user)) {
            throw new BadRequestException("User not assigned to board");
        }

        if (!EnumStateDto.VOTING.equals(board.getState())) {
            throw new BadRequestException("Wrong state of board");
        }

        final int addedUserVotes =
                boardCardsVotesRepository.getVotesByBoardAndUser(board, user)
                        .stream()
                        .reduce(0, Integer::sum);

        final int remainingUserVotes = board.getMaximumNumberOfVotes() - addedUserVotes;
        if (remainingUserVotes == 0) {
            throw new BadRequestException("No more votes");
        }

        boardCardsVotesRepository.findByCardAndVoter(card, user).ifPresentOrElse(vote -> {
            vote.setVotes(vote.getVotes() + 1);
            boardCardsVotesRepository.save(vote);
        }, () -> {
            final BoardCardVotesKey key = new BoardCardVotesKey(card.getId(), user.getUid());
            boardCardsVotesRepository.save(new BoardCardVotes(key, card, user, 1));
        });

        return Map.of("remainingVotes", remainingUserVotes - 1);
    }

    @Transactional
    public Map<String, Integer> removeVote(final Integer cardId, final String email) {

        final User user = userRepository.findUserByEmail(email)
                .orElseThrow(() -> new BadRequestException("User not found"));

        final BoardCard card = boardCardsRepository.findById(cardId)
                .orElseThrow(() -> new NotFoundException("Card not found"));

        if (!card.getBoard().getUsers().contains(user) && !card.getBoard().getCreator().equals(user)) {
            throw new BadRequestException("User not assigned to board");
        }

        if (!EnumStateDto.VOTING.equals(card.getBoard().getState())) {
            throw new BadRequestException("Wrong state of board");
        }

        final BoardCardVotes vote = boardCardsVotesRepository.findByCardAndVoter(card, user)
                .orElseThrow(() -> new BadRequestException("User has no votes to remove"));

        vote.setVotes(vote.getVotes() - 1);
        if (vote.getVotes() == 0) {
            boardCardsVotesRepository.delete(vote);
        }
        return Map.of("remainingVotes", vote.getVotes());
    }
}
