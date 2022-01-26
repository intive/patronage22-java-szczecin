package com.intive.patronage22.szczecin.retroboard.controller;
import com.intive.patronage22.szczecin.retroboard.service.BoardService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@WebMvcTest(BoardController.class)
public class ControllerAndServiceIntegrationTest {
    @Autowired
    private MockMvc mvc;
    
    @SpyBean
    private BoardService service;

    @Test
    public void statusIsOk_TwoObjectsReturned() throws Exception {

        final BoardService boardService = new BoardService();
        mvc.perform(MockMvcRequestBuilders
                        .get("/boards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(2)));
    }
}
