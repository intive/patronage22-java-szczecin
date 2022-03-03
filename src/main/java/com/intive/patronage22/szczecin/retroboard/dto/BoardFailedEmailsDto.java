package com.intive.patronage22.szczecin.retroboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import java.util.List;

@Builder
@Value
@RequiredArgsConstructor
public class BoardFailedEmailsDto {

    @Schema(description = "Failed emails")
    List<String> failedEmails;

    public static BoardFailedEmailsDto createFrom(final List<String> failedEmails) {
        return BoardFailedEmailsDto.builder()
                .failedEmails(failedEmails)
                .build();
    }
}