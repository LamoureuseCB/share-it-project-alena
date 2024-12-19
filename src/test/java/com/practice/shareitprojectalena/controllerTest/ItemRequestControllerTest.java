package com.practice.shareitprojectalena.controllerTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.practice.shareitprojectalena.request.dto.ItemRequestDto;
import com.practice.shareitprojectalena.user.UserRepository;
import com.practice.shareitprojectalena.user.entity.User;
import jakarta.transaction.Transactional;
import lombok.SneakyThrows;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ItemRequestControllerTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @SneakyThrows
    void create_RequestSuccess() {
        User user = new User();
        user.setName("Иван");
        user = userRepository.save(user);

        ItemRequestDto newRequestDto = new ItemRequestDto();
        newRequestDto.setDescription("Электрическая дрель");
        newRequestDto.setRequesterId(user.getId());

        mockMvc.perform(MockMvcRequestBuilders.post("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newRequestDto))).andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.description", Matchers.equalTo("Электрическая дрель")))
                .andExpect(jsonPath("$.requesterId", Matchers.equalTo(user.getId())))
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
    }

    @Test
    @SneakyThrows
    void getAllRequests_ShouldReturnListOfRequests() {
        User user = new User();
        user.setName("Иван");
        user = userRepository.save(user);

        String json = mockMvc.perform(MockMvcRequestBuilders.get("/requests")
                        .header("USER_HEADER", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].requesterId", Matchers.equalTo(user.getId())))
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].description").exists())
                .andExpect(jsonPath("$[0].created").exists())
                .andExpect(jsonPath("$[0].status").exists())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
    }

                
    @Test
    @SneakyThrows
    void update_RequestNotFound() {
        User user = new User();
        user.setName("Иван");
        user = userRepository.save(user);

        mockMvc.perform(MockMvcRequestBuilders.patch("/requests/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ItemRequestDto())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Объект не найден"))
                .andExpect(jsonPath("$.status").value(404))
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
    }

    @Test
    @SneakyThrows
    void update_ThrowForbiddenExceptionWhenUserIsNotOwner() {
        User user = new User();
        user.setName("Иван");
        user = userRepository.save(user);

        mockMvc.perform(MockMvcRequestBuilders.patch("/requests/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ItemRequestDto()))
                        .header("USER_HEADER", user.getId() + 1))
                .andExpect(status().isForbidden())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
    }

}
