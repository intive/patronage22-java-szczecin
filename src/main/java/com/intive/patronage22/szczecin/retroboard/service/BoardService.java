package com.intive.patronage22.szczecin.retroboard.service;
import com.intive.patronage22.szczecin.retroboard.dto.BoardDto;
import com.intive.patronage22.szczecin.retroboard.dto.EnumStateDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;


@Service
public class BoardService {


    public List<BoardDto> mockBoardData() {
        final List<BoardDto> boardDTOS = new ArrayList<>();


        boardDTOS.add(new BoardDto("1a", EnumStateDto.CREATED, "RETRO 1"));
        boardDTOS.add(new BoardDto("2a", EnumStateDto.VOTING, "RETRO 2"));

        return boardDTOS;


    }

}
