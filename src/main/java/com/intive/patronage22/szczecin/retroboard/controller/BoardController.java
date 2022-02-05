package com.intive.patronage22.szczecin.retroboard.controller;

import com.intive.patronage22.szczecin.retroboard.dto.BoardDto;
import com.intive.patronage22.szczecin.retroboard.service.BoardService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("/boards")
public class BoardController {
    @Autowired private BoardService boardService;

    @GetMapping
    public ResponseEntity<List<BoardDto>> getUserBoards(@RequestParam(name = "userId", required = false) String uid) {
        if (uid == null || uid.isBlank()) {
            final List<BoardDto> boards = boardService.mockBoardData();
            return ResponseEntity.status(OK).body(boards);
        }
        
        List<BoardDto> boards = boardService.getUserBoards(uid);
        return ResponseEntity.status(boards == null ? NOT_FOUND : OK).body(boards);
    }
}