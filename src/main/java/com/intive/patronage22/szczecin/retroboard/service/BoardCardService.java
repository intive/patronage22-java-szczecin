package com.intive.patronage22.szczecin.retroboard.service;

import com.intive.patronage22.szczecin.retroboard.dto.BoardCardDto;
import com.intive.patronage22.szczecin.retroboard.dto.BoardCardsColumn;
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

import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardCardService {

    private final BoardCardsRepository boardCardsRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;

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

        if (!boardCard.getCreator().equals(user)
                && !boardCard.getBoard().getCreator().equals(user))
            throw new BadRequestException("User is not allowed to delete card");

        if (!EnumStateDto.CREATED.equals(boardCard.getBoard().getState()))
            throw new BadRequestException("User is not allowed to delete card");

        boardCardsRepository.deleteById(cardId);
    }
}
