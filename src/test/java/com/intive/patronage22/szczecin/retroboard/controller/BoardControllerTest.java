package com.intive.patronage22.szczecin.retroboard.controller;

import com.intive.patronage22.szczecin.retroboard.model.User;
import com.intive.patronage22.szczecin.retroboard.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BoardControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;


    @Test
    void userAlreadyExists_shouldReturnStatus201() throws Exception {
        // given
        User user = new User("uid101", "Josef");
        userRepository.save(user);

        // when
        var action = mockMvc.perform(
                post("/boards?userId={id}", "uid101")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"name\": \"test\"}")
                        .accept(MediaType.APPLICATION_JSON));

        // then
        action.andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("test"));
    }

    @Test
    void userNotExists_shouldReturnStatus404() throws Exception {
        // given
        // when
        var action = mockMvc.perform(
                post("/boards?userId={id}", "uid102").contentType(
                                MediaType.APPLICATION_JSON)
                        .content("{ \"name\": \"test\"}")
                        .accept(MediaType.APPLICATION_JSON));

        // then
        action.andExpect(status().isNotFound());
    }
}