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
import java.util.concurrent.atomic.AtomicReference;

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
                .board(board).column(BoardCardsColumn.columnIdToBoardCardsColumn(boardCardDto.getColumnId()))
                .text(boardCardDto.getCardText()).creator(user).boardCardActions(List.of()).build();

        boardCardsRepository.save(boardCard);

        return BoardCardDto.createFrom(boardCard);
    }

    @Transactional
    public Map<String, Integer> vote(final Integer cardId, final String email) {
        final User user =
                userRepository.findUserByEmail(email).orElseThrow(() -> new BadRequestException("User not found"));

        final BoardCard card =
                boardCardsRepository.findById(cardId).orElseThrow(() -> new NotFoundException("Card not found"));

        final Board board = boardRepository.findById(card.getBoard().getId())
                .orElseThrow(() -> new BadRequestException("Board not exist"));

        if (! board.getUsers().contains(user) || ! board.getCreator().equals(user)) {
            throw new BadRequestException("User is not assigned to board");
        }

        if (! board.getState().equals(EnumStateDto.VOTING)) {
            throw new BadRequestException("Wrong state of board");
        }

        AtomicReference<Integer> addedUserVotes = new AtomicReference<>(0);

        final List<BoardCard> boardCards = boardCardsRepository.findAllByBoardIdOrderByIdAsc(board.getId());

        for (BoardCard boardCard : boardCards) {
            boardCardsVotesRepository.findByCardAndVoter(boardCard, user)
                    .ifPresent(vote -> addedUserVotes.updateAndGet(v -> v + vote.getVotes()));
        }

        final int remainingUserVotes = board.getMaximumNumberOfVotes() - addedUserVotes.get();
        if (remainingUserVotes == 0) {
            throw new BadRequestException("No more votes");
        }

        boardCardsVotesRepository.findByCardAndVoter(card, user).ifPresentOrElse(vote -> vote.setVotes(vote.getVotes() + 1), () -> {
            final BoardCardVotesKey key = new BoardCardVotesKey(card.getId(), user.getUid());
            boardCardsVotesRepository.save(new BoardCardVotes(key, card, user, 1));
        });

        return Map.of("remainingVotes", remainingUserVotes - 1);
    }

}
