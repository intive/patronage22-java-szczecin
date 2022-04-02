package com.intive.patronage22.szczecin.retroboard.dto;

import com.intive.patronage22.szczecin.retroboard.exception.NotAcceptableException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.jupiter.api.Assertions.assertThrows;

class EnumStateDtoTest {

    @Test
    void setNextStateShouldReturnNotAcceptableIfEnumStateIsDone() {
        // given
        final EnumStateDto enumStateDto = EnumStateDto.DONE;

        // when & then
        assertThrows(NotAcceptableException.class, enumStateDto::next);
    }
}
