package com.intive.patronage22.szczecin.retroboard.validation;

import com.intive.patronage22.szczecin.retroboard.dto.BoardPatchDto;
import com.intive.patronage22.szczecin.retroboard.exception.InvalidArgumentException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.nonNull;

@Component
@RequiredArgsConstructor
public class BoardValidator {

    private static final int MAX_NAME_LENGTH = 32;

    public void validateBoardParameters(final BoardPatchDto boardPatchDto) throws InvalidArgumentException {
        final var fieldErrors = getFieldErrors(boardPatchDto);

        if (!fieldErrors.isEmpty()) {

            throw new InvalidArgumentException(fieldErrors);
        }
    }

    private List<FieldError> getFieldErrors(final BoardPatchDto boardPatchDto) {


        return Optional.ofNullable(boardPatchDto)
                .map(boardPatch -> Stream
                        .of(checkName(boardPatch.getName()),
                                checkMaximumNumberOfVotes(boardPatch.getMaximumNumberOfVotes()))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }

    private FieldError checkName(final String name) {
        if (nonNull(name) && (name.isBlank() || name.length() > MAX_NAME_LENGTH)) {
            final String message = "Name cannot be blank or null and can be up to 32 characters";
            return new FieldError("String", "name",
                    name, false, null, null, message);
        } else {
            return null;
        }
    }

    private FieldError checkMaximumNumberOfVotes(final Integer maxVotes) {
        if (nonNull(maxVotes) && maxVotes < 0) {
            final String message = "Value cannot be above " + Integer.MAX_VALUE + " and value cannot be below 0.";
            return new FieldError("String", "maximumNumberOfVotes",
                    maxVotes, false, null, null, message);
        } else {
            return null;
        }

    }

}
