package com.intive.patronage22.szczecin.retroboard.controller;
import com.intive.patronage22.szczecin.retroboard.repository.UserRepository;
import com.intive.patronage22.szczecin.retroboard.service.BoardService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@WebMvcTest(BoardController.class)
public class ControllerAndServiceIntegrationTest {
    @Autowired
    private MockMvc mvc;
    
    @SpyBean
    private BoardService service;

    @MockBean private UserRepository userRepository;

    @Test
    public void whenNoUserGiven_thenBadRequestStatus() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/boards?userId=")).
                andExpect(status().isBadRequest()).
                andExpect(result -> assertTrue(result.getResolvedException() instanceof ResponseStatusException));
    }

    @Test
    public void whenNoSuchUser_thenNotFoundStatus() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/boards?userId=nosuchuserid")).
                andExpect(status().isNotFound()).
                andExpect(result -> assertTrue(result.getResolvedException() instanceof ResponseStatusException));
    }
}
