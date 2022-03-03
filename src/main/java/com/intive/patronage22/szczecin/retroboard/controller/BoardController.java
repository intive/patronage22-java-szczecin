package com.intive.patronage22.szczecin.retroboard.controller;

import com.intive.patronage22.szczecin.retroboard.dto.BoardDataDto;
import com.intive.patronage22.szczecin.retroboard.dto.BoardDto;
import com.intive.patronage22.szczecin.retroboard.dto.BoardPatchDto;
import com.intive.patronage22.szczecin.retroboard.service.BoardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/v1/boards", produces = MediaType.APPLICATION_JSON_VALUE)
public class BoardController {

    private final BoardService boardService;

    @GetMapping
    @ResponseStatus(OK)
    @Operation(security = @SecurityRequirement(name = "tokenAuth"), summary = "Get retro board for given user.",
            responses = {@ApiResponse(responseCode = "200", description = "OK"),
                    @ApiResponse(responseCode = "400", description = "Bad request data"),
                    @ApiResponse(responseCode = "404", description = "User not found")})
    public List<BoardDto> getUserBoards(final Authentication authentication) {
        return boardService.getUserBoards(authentication.getName());
    }

    @GetMapping("/{id}/details")
    @ResponseStatus(OK)
    @Operation(security = @SecurityRequirement(name = "tokenAuth"), summary = "Get retro board details for user by id",
               responses = {@ApiResponse(responseCode = "200", description = "OK"),
                       @ApiResponse(responseCode = "400", description = "User has no access to board."),
                       @ApiResponse(responseCode = "404", description = "Board not found")})
    public List<BoardDetailsDto> getBoardDetailsById(@PathVariable final Integer id,
                                                                  final Authentication authentication) {

        return boardService.getBoardDetailsById(id, authentication.getName());
    }

    @GetMapping("/{id}")
    @ResponseStatus(OK)
    @Operation(security = @SecurityRequirement(name = "tokenAuth"), summary = "Get retro board data for user by id",
               responses = {@ApiResponse(responseCode = "200", description = "OK"),
                       @ApiResponse(responseCode = "400", description = "User has no access to board."),
                       @ApiResponse(responseCode = "404", description = "Board not found")})
    public BoardDataDto getBoardDataById(@PathVariable final Integer id, final Authentication authentication) {

        return boardService.getBoardDataById(id, authentication.getName());
    }

    @PostMapping
    @ResponseStatus(CREATED)
    @Operation(security = @SecurityRequirement(name = "tokenAuth"), summary = "Create retro board for given user.",
            responses = {@ApiResponse(responseCode = "201", description = "Board created for given user"),
                    @ApiResponse(responseCode = "400", description = "User not found"),
                    @ApiResponse(responseCode = "400", description = "Board name not valid")})
    public BoardDto createBoard(@RequestBody @Valid final BoardDto boardDto,
                                final Authentication authentication) {

        return boardService.createBoard(boardDto.getName(), authentication.getName());
    }

    @PostMapping("/{id}/users")
    @ResponseStatus(CREATED)
    @Operation(security = @SecurityRequirement(name = "tokenAuth"), summary = "Assign users to given board.",
               responses = {@ApiResponse(responseCode = "201", description = "Users assigned to the board"),
                       @ApiResponse(responseCode = "400", description = "User is not the board owner"),
                       @ApiResponse(responseCode = "404", description = "Board/user not found")})
    public ResponseEntity<List<String>> assignUsersToBoard(@PathVariable final Integer id,
                                                           @RequestBody final List<String> usersEmails,
                                                           final Authentication authentication) {
        return ResponseEntity.status(CREATED)
                .body(boardService.assignUsersToBoard(id, usersEmails, authentication.getName()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(OK)
    @Operation(security = @SecurityRequirement(name = "tokenAuth"), summary = "Delete retro board for given id.",
            responses = {@ApiResponse(responseCode = "200", description = "Board deleted for given user"),
                    @ApiResponse(responseCode = "400", description = "User is not the board owner"),
                    @ApiResponse(responseCode = "404", description = "Board not found")})
    public void deleteBoard(@PathVariable(name = "id") final int id,
                            final Authentication authentication) {

        boardService.delete(id, authentication.getName());
    }

    @PatchMapping("/{id}")
    @Operation(security = @SecurityRequirement(name = "tokenAuth"), summary = "Update board name and number of votes")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Bad request data"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<BoardDto> update(@PathVariable("id") final Integer id,
                                           @RequestBody final BoardPatchDto boardPatchDto,
                                           final Authentication authentication) {
        final BoardDto boardDto = boardService.patchBoard(id, boardPatchDto, authentication.getName());
        return ResponseEntity.status(OK).body(boardDto);
    }

    @DeleteMapping("/{id}/users/{uid}")
    @Operation(security = @SecurityRequirement(name = "tokenAuth"), summary = "Remove user from board.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description =
                    "User wants to remove another user or creator wants to remove himself from the board"),
            @ApiResponse(responseCode = "404", description = "Board not exist or user not  assigned to the board.")
    })
    public void deleteAssignedUser(@PathVariable("uid") final String uid,
                                   @PathVariable("id") final Integer id,
                                   final Authentication authentication) {
        boardService.removeUserAssignedToTheBoard(uid, id, authentication.getName());
    }

    @PostMapping("/{id}/nextState")
    @Operation(security = @SecurityRequirement(name = "tokenAuth"), summary = "Next state of the board.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Bad request data"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "406", description = "No more steps")
    })
    public BoardDataDto setNextState(@PathVariable("id") final Integer id, final Authentication authentication) {
        return boardService.setNextState(id, authentication.getName());
    }
}