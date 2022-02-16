package com.intive.patronage22.szczecin.retroboard.controller;

import com.intive.patronage22.szczecin.retroboard.dto.BoardDto;
import com.intive.patronage22.szczecin.retroboard.dto.BoardCreateDto;
import com.intive.patronage22.szczecin.retroboard.service.BoardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/boards", produces = MediaType.APPLICATION_JSON_VALUE)
public class BoardController {
    private final BoardService boardService;

    @GetMapping
    @ResponseStatus(OK)
    @Operation(summary = "Get retro board for given user.",
               responses = {@ApiResponse(responseCode = "200", description = "OK"),
                            @ApiResponse(responseCode = "400", description = "Bad request data"),
                            @ApiResponse(responseCode = "404", description = "User not found")})
    public List<BoardDto> getUserBoards(@RequestParam(name = "userId") final String uid) {
        return boardService.getUserBoards(uid);
    }

    @PostMapping
    @ResponseStatus(CREATED)
    @Operation(summary = "Create retro board for given user.",
            responses = {@ApiResponse(responseCode = "201", description = "Board created for given user"),
                         @ApiResponse(responseCode = "404", description = "User not found")})
    public BoardDto createNewBoard(@RequestParam(name = "userId") final String uid,
                                   @RequestBody final BoardCreateDto boardName) {

        return boardService.createNewBoard(boardName.getName(), uid);
    }
}