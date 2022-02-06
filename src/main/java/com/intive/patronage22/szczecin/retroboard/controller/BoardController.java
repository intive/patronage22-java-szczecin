package com.intive.patronage22.szczecin.retroboard.controller;

import com.intive.patronage22.szczecin.retroboard.dto.BoardDto;
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
    public ResponseEntity<List<BoardDto>> getBoards() {
        final List<BoardDto> boards = boardService.mockBoardData();

        return ResponseEntity.status(OK).body(boards);
    }
}