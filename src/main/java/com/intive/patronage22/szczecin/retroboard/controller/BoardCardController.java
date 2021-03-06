package com.intive.patronage22.szczecin.retroboard.controller;

import com.intive.patronage22.szczecin.retroboard.dto.BoardCardActionDto;
import com.intive.patronage22.szczecin.retroboard.dto.BoardCardActionRequestDto;
import com.intive.patronage22.szczecin.retroboard.dto.BoardCardDto;
import com.intive.patronage22.szczecin.retroboard.service.BoardCardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

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

    @DeleteMapping("/{id}")
    @ResponseStatus(OK)
    @Operation(security = @SecurityRequirement(name = "tokenAuth"), summary = "Remove card from the board.",
               responses = {@ApiResponse(responseCode = "200", description = "Card successfully removed"),
                    @ApiResponse(responseCode = "400", description = "User is not allowed to delete card"),
                    @ApiResponse(responseCode = "404", description = "Card not found")})
    public void removeCardFromTheBoard(@PathVariable(name = "id") final Integer cardId,
                                       final Authentication authentication) {

        boardCardService.removeCard(cardId, authentication.getName());
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

    @DeleteMapping("/{id}/votes")
    @ResponseStatus(OK)
    @Operation(security = @SecurityRequirement(name = "tokenAuth"), summary = "Remove vote",
            responses = {@ApiResponse(responseCode = "200", description = "Vote removed"),
                    @ApiResponse(responseCode = "400",
                                description = "User has no access to board or there is no vote to remove"),
                    @ApiResponse(responseCode = "404",
                                description = "Card not exist")
    })
    public Map<String, Integer> removeVote(@PathVariable(name = "id") final Integer cardId,
                                        final Authentication authentication) {

        return boardCardService.removeVote(cardId, authentication.getName());
    }

    @PostMapping("/{id}/actions")
    @ResponseStatus(CREATED)
    @Operation(security = @SecurityRequirement(name = "tokenAuth"), summary = "Add Card Action",
            responses = {@ApiResponse(responseCode = "201", description = "Card action added"),
                    @ApiResponse(responseCode = "400",
                            description = "User is not the owner of the board/state is not actions."),
                    @ApiResponse(responseCode = "404",
                            description = "Card does not exist")
            })
    public BoardCardActionDto addCardAction(
            @PathVariable(name = "id") final Integer cardId,
            @RequestBody @Valid final BoardCardActionRequestDto boardCardActionText,
            final Authentication authentication) {
        return boardCardService.addCardAction(cardId, authentication.getName(), boardCardActionText);
    }

    @DeleteMapping("actions/{id}")
    @ResponseStatus(OK)
    @Operation(security = @SecurityRequirement(name = "tokenAuth"), summary = "Remove vote",
            responses = {@ApiResponse(responseCode = "200", description = "Action removed"),
                    @ApiResponse(responseCode = "400",
                            description = "No ownership or board's state is not ACTIONS"),
                    @ApiResponse(responseCode = "404", description = "Action not found")})
    public void removeActionFromTheCard(@PathVariable(name = "id") final Integer actionId,
                                        final Authentication authentication) {

        boardCardService.removeAction(actionId, authentication.getName());
    }
}