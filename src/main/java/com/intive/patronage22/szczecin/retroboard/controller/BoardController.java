package com.intive.patronage22.szczecin.retroboard.controller;

import com.intive.patronage22.szczecin.retroboard.dto.BoardDataDto;
import com.intive.patronage22.szczecin.retroboard.dto.BoardDto;
import com.intive.patronage22.szczecin.retroboard.dto.BoardPatchDto;
import com.intive.patronage22.szczecin.retroboard.service.BoardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
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

    @GetMapping("/{id}")
    @ResponseStatus(OK)
    @Operation(summary = "Get retro board data for user by id",
               responses = {@ApiResponse(responseCode = "200", description = "OK"),
                       @ApiResponse(responseCode = "400", description = "User has no access to board."),
                       @ApiResponse(responseCode = "404", description = "Board is not found")})
    public BoardDataDto getBoardDataById(@PathVariable final Integer id, final Authentication authentication) {

        return boardService.getBoardDataById(id, authentication.getName());
    }

    @PostMapping
    @ResponseStatus(CREATED)
    @Operation(summary = "Create retro board for given user.",
            responses = {@ApiResponse(responseCode = "201", description = "Board created for given user"),
                    @ApiResponse(responseCode = "404", description = "User not found"),
                    @ApiResponse(responseCode = "400", description = "Board name not valid")})
    public BoardDto createBoard(@RequestParam(name = "userId") final String uid,
                                @RequestBody @Valid final BoardDto boardDto) {
        return boardService.createBoard(boardDto.getName(), uid);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(OK)
    @Operation(summary = "Delete retro board for given id.",
            responses = {@ApiResponse(responseCode = "200", description = "Board deleted for given user"),
                    @ApiResponse(responseCode = "400", description = "User is not the board owner"),
                    @ApiResponse(responseCode = "404", description = "Board not found")})
    public void deleteBoard(@RequestParam(name = "userId") final String uid,
                            @PathVariable(name = "id") final int id){

        boardService.delete(id, uid);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update board name and number of votes")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Bad request data"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })

    public ResponseEntity<BoardDto> update(
            @PathVariable("id") final Integer id,
            @RequestParam(name = "userId") final String uid,
            @RequestBody final BoardPatchDto boardPatchDto) {
        final BoardDto boardDto = boardService.patchBoard(id, boardPatchDto, uid);
        return ResponseEntity.status(OK).body(boardDto);
    }
}