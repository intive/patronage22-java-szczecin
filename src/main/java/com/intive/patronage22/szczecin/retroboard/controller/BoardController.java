package com.intive.patronage22.szczecin.retroboard.controller;


import com.intive.patronage22.szczecin.retroboard.dto.BoardDTO;
import com.intive.patronage22.szczecin.retroboard.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.http.HttpStatus.OK;


@RequiredArgsConstructor
@RestController
@RequestMapping("/boards")
public class BoardController {


private final BoardService boardService;

@GetMapping
    public ResponseEntity<List<BoardDTO>> getBoards(){
        final List<BoardDTO> boards = boardService.mockBoardData();
        return ResponseEntity.status(OK).body(boards);

    }
}