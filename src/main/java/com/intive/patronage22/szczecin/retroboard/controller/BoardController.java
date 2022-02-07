package com.intive.patronage22.szczecin.retroboard.controller;

import com.intive.patronage22.szczecin.retroboard.dto.BoardDto;
import com.intive.patronage22.szczecin.retroboard.service.BoardService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;
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

    @PostMapping
    @ResponseStatus(CREATED)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Creation successful"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public BoardDto createNewBoardForUserId(@RequestParam String userId,
                                            @RequestBody BoardNameDTO nameBoard) {

        return boardService.saveBoardForUserId(nameBoard.getName(), userId);
    }
}