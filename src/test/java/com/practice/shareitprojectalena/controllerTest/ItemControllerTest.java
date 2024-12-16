package com.practice.shareitprojectalena.controllerTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.practice.shareitprojectalena.item.Item;
import com.practice.shareitprojectalena.item.itemDto.ItemCreateDto;
import com.practice.shareitprojectalena.item.itemDto.ItemUpdateDto;
import jakarta.transaction.Transactional;
import lombok.SneakyThrows;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ItemControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;


    private ItemCreateDto itemCreateDto;
    private Long userId = 1L;

    @BeforeEach
    public void setUp() {

    }

    @Test
    @SneakyThrows
    public void testCreateItemSuccess() {
        itemCreateDto = new ItemCreateDto();
        itemCreateDto.setName("Тестовая вещь");
        itemCreateDto.setDescription("Тестовое описание");
        itemCreateDto.setAvailable(true);
        itemCreateDto.setRequestId(null);
        Item item = new Item();
        item.setName(itemCreateDto.getName());
        item.setDescription(itemCreateDto.getDescription());
        item.setIsAvailable(itemCreateDto.getAvailable());


        mockMvc.perform(post("/items")
                        .header("USER_HEADER", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemCreateDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Тестовая вещь"))
                .andExpect(jsonPath("$.description").value("Тестовое описание"))
                .andExpect(jsonPath("$.available").value(true))
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        ;
    }

    @Test
    @SneakyThrows
    public void testCreateItemUserNotFound() {
        itemCreateDto = new ItemCreateDto();
        itemCreateDto.setName("Тестовая вещь");
        itemCreateDto.setDescription("Тестовое описание");
        itemCreateDto.setAvailable(true);
        itemCreateDto.setRequestId(null);
        mockMvc.perform(post("/items")
                        .header("USER_HEADER", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemCreateDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Пользователь по данному ID не найден"))
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        ;
    }

    @Test
    @SneakyThrows
    public void testCreateItemRequestNotFound() {
        itemCreateDto = new ItemCreateDto();
        itemCreateDto.setName("Тестовая вещь");
        itemCreateDto.setDescription("Тестовое описание");
        itemCreateDto.setAvailable(true);
        itemCreateDto.setRequestId(null);
        mockMvc.perform(post("/items")
                        .header("USER_HEADER", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemCreateDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Запрос не найден"))
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        ;
    }


    @Test
    @SneakyThrows
    public void testUpdateItemSuccess() {
        Long itemId = 1L;
        ItemUpdateDto itemUpdateDto = new ItemUpdateDto();
        itemUpdateDto.setName("Обновленная вещь");
        itemUpdateDto.setDescription("Обновленное описание");
        itemUpdateDto.setIsAvailable(false);

        String json = mockMvc.perform(patch("/items/{itemId}", itemId)
                        .header("USER_HEADER", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemUpdateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", Matchers.equalTo("Обновленная вещь")))
                .andExpect(jsonPath("$.description", Matchers.equalTo("Обновленное описание")))
                .andExpect(jsonPath("$.available", Matchers.equalTo(false)))
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
    }

    @Test
    @SneakyThrows
    public void testUpdateUnsuccessfully() {
        Long itemId = 100999L;
        ItemUpdateDto itemUpdateDto = new ItemUpdateDto();
        itemUpdateDto.setName("Вещь с несуществующим ID");
        itemUpdateDto.setDescription("Описание вещи с несуществующим ID");
        itemUpdateDto.setIsAvailable(false);

        mockMvc.perform(patch("/items/{itemId}", itemId)
                        .header("USER_HEADER", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemUpdateDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Объект не найден"))
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
    }

    @Test
    @SneakyThrows
    public void testDeleteItemSuccessfully() {
        Long itemId = 1L;
        mockMvc.perform(delete("/items/{itemId}", itemId)
                        .header("USER_HEADER", userId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
    }

    @Test
    @SneakyThrows
    public void testDeleteItemUnsuccessfully() {
        Long itemId = 100999L;
        mockMvc.perform(delete("/items/{itemId}", itemId)
                        .header("USER_HEADER", userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Объект не найден"))
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
    }

    @Test
    @SneakyThrows
    public void testSearchItemsSuccessfully() {
        String text = "само";
        int from = 0;
        int size = 10;

        mockMvc.perform(get("/items/search")
                        .param("text", text)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", Matchers.hasSize(10)))
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
    }
}
