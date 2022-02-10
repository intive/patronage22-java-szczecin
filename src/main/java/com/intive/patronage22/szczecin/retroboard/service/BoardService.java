package com.intive.patronage22.szczecin.retroboard.service;

import com.intive.patronage22.szczecin.retroboard.dto.BoardDto;
import com.intive.patronage22.szczecin.retroboard.dto.EnumStateDto;
import com.intive.patronage22.szczecin.retroboard.model.User;
import com.intive.patronage22.szczecin.retroboard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BoardService {
    private final UserRepository userRepository;

    @Transactional
    public List<BoardDto> getUserBoards(String uid) {
        if (uid == null || uid.isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid request - no uid given!");

        User u = userRepository.findById(uid).orElseThrow(
                () -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "No such user!"));

        return u.getUserBoards().stream().map(b -> BoardDto.toDto(b))
                .collect(Collectors.toList());
    }

    public List<BoardDto> mockBoardData() {
        final List<BoardDto> boardDTOS = new ArrayList<>();
        boardDTOS.add(new BoardDto(1, EnumStateDto.CREATED, "RETRO 1"));
        boardDTOS.add(new BoardDto(2, EnumStateDto.VOTING, "RETRO 2"));

        return boardDTOS;
    }
}
