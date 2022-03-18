package com.intive.patronage22.szczecin.retroboard.controller;

import com.intive.patronage22.szczecin.retroboard.dto.BoardCardDto;
import com.intive.patronage22.szczecin.retroboard.service.BoardCardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Map;

import static org.springframework.http.HttpStatus.CREATED;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/v1/cards", produces = MediaType.APPLICATION_JSON_VALUE)
public class BoardCardController {

    private final BoardCardService boardCardService;

    @PostMapping("/boards/{id}")
    @ResponseStatus(CREATED)
    @Operation(security = @SecurityRequirement(name = "tokenAuth"), summary = "Add card to the board.",
            responses = {@ApiResponse(responseCode = "201", description = "Board card created for given board"),
                    @ApiResponse(responseCode = "400", description = "User is not found, not the owner/or not assigned "
                            + "to the board, board state is not CREATED or posted object is incorrect/not valid."),
                    @ApiResponse(responseCode = "404", description = "Board not found")})
    public BoardCardDto addCardToTheBoard(@RequestBody @Valid final BoardCardDto boardCardDto,
                                          @PathVariable(name = "id") final Integer boardId,
                                          final Authentication authentication) {

        return boardCardService.createBoardCard(boardCardDto, boardId, authentication.getName());
    }

    @PostMapping("/{id}/votes")
    @ResponseStatus(CREATED)
    @Operation(security = @SecurityRequirement(name = "tokenAuth"), summary = "Add card to the board.",
               responses = {@ApiResponse(responseCode = "201", description = "Voted"),
                       @ApiResponse(responseCode = "400",
                                    description = "No more votes. Board state is not in state VOTING. User is not " +
                                                  "found. Board is not found. " +
                                                  "User is not assigned to board nor owner."),
                       @ApiResponse(responseCode = "404", description = "Card not found")})
    public Map<String, Integer> addVote(@PathVariable(name = "id") final Integer cardId,
                                        final Authentication authentication) {

        return boardCardService.addVote(cardId, authentication.getName());
    }
}
