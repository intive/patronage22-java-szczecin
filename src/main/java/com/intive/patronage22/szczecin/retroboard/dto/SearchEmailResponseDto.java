package com.intive.patronage22.szczecin.retroboard.dto;

import com.intive.patronage22.szczecin.retroboard.model.User;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.util.List;
import java.util.stream.Collectors;

@Value
@Builder
@RequiredArgsConstructor
public class SearchEmailResponseDto {

    List<String> email;

    public static SearchEmailResponseDto createFrom(final List<User> users) {
         return SearchEmailResponseDto.builder()
                 .email(returnListWithEmails(users))
                 .build();
    }

    public static SearchEmailResponseDto createFrom() {
        return SearchEmailResponseDto.builder()
                .email(List.of())
                .build();
    }

    private static List<String> returnListWithEmails(final List<User> users) {
        return users.stream()
                .map(User::getEmail)
                .collect(Collectors.toList());
    }
}
