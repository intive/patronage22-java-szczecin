package com.intive.patronage22.szczecin.retroboard.service;

import com.intive.patronage22.szczecin.retroboard.dto.BoardDto;
import com.intive.patronage22.szczecin.retroboard.model.Board;
import com.intive.patronage22.szczecin.retroboard.model.User;
import com.intive.patronage22.szczecin.retroboard.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
public class BoardService {
    @Autowired
    private UserRepository userRepository;

    private final ModelMapper modelMapper = new ModelMapper();

    @Transactional
    public List<BoardDto> getUserBoards(String uid) {
        User u = userRepository.findById(uid).orElse(null);
        if (u == null)
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "No such user!");
        List<BoardDto> boards = new ArrayList<>();
        for (Board board : u.getUserBoards()) {
            BoardDto boardDto = modelMapper.map(board, BoardDto.class);
            boards.add(boardDto);
        }
        return boards;
    }
}
