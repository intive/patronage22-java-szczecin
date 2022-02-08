package com.intive.patronage22.szczecin.retroboard.service;

import com.intive.patronage22.szczecin.retroboard.dto.BoardDto;
import com.intive.patronage22.szczecin.retroboard.dto.EnumStateDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class BoardService {

    public List<BoardDto> mockBoardData() {
        final List<BoardDto> boardDTOS = new ArrayList<>();
        boardDTOS.add(new BoardDto(1, EnumStateDto.CREATED, "RETRO 1"));
        boardDTOS.add(new BoardDto(2, EnumStateDto.VOTING, "RETRO 2"));

        return boardDTOS;
    }

    @Transactional
    public BoardDto saveBoardForUserId(String boardName, String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Board newBoard = new Board(boardName, user);
        Board save = boardRepository.save(newBoard);
        return BoardDto.mapToDto(save);
    }
}
