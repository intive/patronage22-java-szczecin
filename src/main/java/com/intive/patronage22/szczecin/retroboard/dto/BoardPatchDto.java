package com.intive.patronage22.szczecin.retroboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@Builder
@Value
@RequiredArgsConstructor
public class BoardPatchDto {

    @Schema(description = "Board name", required = true)
    String name;

    @Schema(description = "Maximum number of votes")
    Integer maximumNumberOfVotes;
}
