package com.intive.patronage22.szczecin.retroboard.validation;

import com.intive.patronage22.szczecin.retroboard.dto.BoardPatchDto;
import com.intive.patronage22.szczecin.retroboard.exception.InvalidArgumentException;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = BoardValidator.class)
class BoardValidatorTest {

    @Autowired
    private BoardValidator boardValidator;

    @ParameterizedTest
    @MethodSource("validBoardNames")
    void validateBoardNamePassesThroughWhenNameIsValid(final BoardPatchDto boardPatchDto) {
        assertDoesNotThrow(() -> boardValidator.validateBoardParameters(boardPatchDto));
    }

    @ParameterizedTest
    @MethodSource("invalidBoardNames")
    void validateBoardNamesThrowsWhenBoardNameInvalid(final BoardPatchDto boardPatchDto) {
        assertThrows(InvalidArgumentException.class, () -> boardValidator.validateBoardParameters(boardPatchDto));
    }

    private static Stream<BoardPatchDto> validBoardNames() {
        return Stream.of(
                new BoardPatchDto("test board name", 0),
                new BoardPatchDto("tęśt bóąrd nąmę", 0),
                new BoardPatchDto("!@#$@#%$^&%*", 0),
                new BoardPatchDto("1", 0));
    }

    private static Stream<BoardPatchDto> invalidBoardNames() {
        return Stream.of(new BoardPatchDto
                ("252435245234524352354234234235235324523452234542353245325432543245235", 0),
        new BoardPatchDto(" ", 0));
    }
}
