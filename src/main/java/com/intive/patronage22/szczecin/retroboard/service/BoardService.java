package com.intive.patronage22.szczecin.retroboard.service;
import com.intive.patronage22.szczecin.retroboard.dto.BoardDto;
import com.intive.patronage22.szczecin.retroboard.dto.EnumStateDto;
import com.intive.patronage22.szczecin.retroboard.model.Board;
import com.intive.patronage22.szczecin.retroboard.model.User;
import com.intive.patronage22.szczecin.retroboard.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;


@Transactional
@Service
public class BoardService {
    @Autowired
    private UserRepository userRepository;

    private final ModelMapper modelMapper = new ModelMapper();

    public List<BoardDto> mockBoardData() {
        final List<BoardDto> boardDTOS = new ArrayList<>();


        boardDTOS.add(new BoardDto(1, EnumStateDto.CREATED, "RETRO 1"));
        boardDTOS.add(new BoardDto(2, EnumStateDto.VOTING, "RETRO 2"));

        return boardDTOS;


    }

    public List<BoardDto> getUserBoards(String uid) {
        User u = userRepository.findById(uid).orElse(null);
        if (u == null)
            return null;
        List<BoardDto> boards = new ArrayList<>();
        for (Board board : u.getUserBoards()) {
            BoardDto boardDto = modelMapper.map(board, BoardDto.class);
            boards.add(boardDto);
        }
        return boards;
    }
}
