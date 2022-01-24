package com.intive.patronage22.szczecin.retroboard.service;


import com.intive.patronage22.szczecin.retroboard.dto.BoardDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardService {


    public List<BoardDTO> mockBoardData (){
        final List<BoardDTO> boardDTOS = new ArrayList<>();




            boardDTOS.add(new BoardDTO("1a", BoardDTO.State.CREATED,"RETRO 1"));
            boardDTOS.add(new BoardDTO("2a", BoardDTO.State.VOTING,"RETRO 2"));

return boardDTOS;


    }

}
