package com.intive.patronage22.szczecin.retroboard.dto;
import com.intive.patronage22.szczecin.retroboard.model.BoardCardVotes;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@Builder
@Value
@RequiredArgsConstructor
public class BoardCardVotesDto {

    @Schema(description = "Board card user") String userUid;

    public static BoardCardVotesDto fromModel(final BoardCardVotes boardCardVotes) {
        return BoardCardVotesDto.builder().userUid(boardCardVotes.getVoter().getUid()).build();
    }
}
