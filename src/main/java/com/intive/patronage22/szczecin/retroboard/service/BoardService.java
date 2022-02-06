package com.intive.patronage22.szczecin.retroboard.service;

import com.intive.patronage22.szczecin.retroboard.dto.BoardDto;
import com.intive.patronage22.szczecin.retroboard.dto.EnumStateDto;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class BoardService {

    public List<BoardDto> mockBoardData() {
        final List<BoardDto> boardDTOS = new ArrayList<>();
        boardDTOS.add(new BoardDto("1a", EnumStateDto.CREATED, "RETRO 1"));
        boardDTOS.add(new BoardDto("2a", EnumStateDto.VOTING, "RETRO 2"));

        return boardDTOS;
    }

    public BoardDto saveBoard(BoardCreateDto boardName, String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Board newBoard = new Board(boardName.getName(), user);
        Board save = boardRepository.save(newBoard);
        return BoardDto.mapToDto(save);
    }
}
