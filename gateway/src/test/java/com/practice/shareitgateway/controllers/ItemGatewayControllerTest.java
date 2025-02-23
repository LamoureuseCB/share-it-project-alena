package com.practice.shareitgateway.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.practice.shareitgateway.error.exceptions.NotFoundException;
import com.practice.shareitgateway.item.ItemGatewayController;
import com.practice.shareitgateway.item.itemDto.ItemCreateDto;
import com.practice.shareitgateway.item.itemDto.ItemResponseDto;
import com.practice.shareitgateway.item.itemDto.ItemUpdateDto;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static com.practice.shareitgateway.utils.RequestConstants.SERVER_URL;
import static com.practice.shareitgateway.utils.RequestConstants.USER_HEADER;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemGatewayController.class)
public class ItemGatewayControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    private RestTemplate restTemplate;

    private final Long userId = 2L;

    private ItemCreateDto createDto() {
        ItemCreateDto itemCreateDto = new ItemCreateDto();
        itemCreateDto.setName("test");
        itemCreateDto.setDescription("description");
        itemCreateDto.setAvailable(true);
        Long requestId = 1L;
        itemCreateDto.setRequestId(requestId);
        return itemCreateDto;
    }

    private ItemResponseDto createResponseDto() {
        ItemResponseDto itemResponseDto = new ItemResponseDto();
        itemResponseDto.setId(createDto().getRequestId());
        itemResponseDto.setName(createDto().getName());
        itemResponseDto.setDescription(createDto().getDescription());
        itemResponseDto.setAvailable(createDto().getAvailable());
        return itemResponseDto;
    }

    @Test
    @SneakyThrows
    public void createItem_Success_ReturnItemResponse() {
        ItemCreateDto itemCreateDto = createDto();
        ItemResponseDto itemResponseDto = createResponseDto();

        when(restTemplate.postForObject(
                eq(SERVER_URL + "/items"),
                any(HttpEntity.class),
                eq(ItemResponseDto.class)))
                .thenReturn(itemResponseDto);

        mockMvc.perform(post("/items")
                        .header(USER_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemCreateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(itemResponseDto.getName()))
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
    }

    @Test
    @SneakyThrows
    public void createItem_userIdNull_ThrowException() {
        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
    }

    @Test
    @SneakyThrows
    public void createItem_InvalidRequestBody_ThrowException() {
        ItemCreateDto invalidCreateDto = new ItemCreateDto();

        mockMvc.perform(post("/items")
                        .header(USER_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidCreateDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    public void createItem_ServerError_ThrowException() {
        ItemCreateDto itemCreateDto = createDto();

        when(restTemplate.postForObject(
                eq(SERVER_URL + "/items"),
                any(HttpEntity.class),
                eq(ItemResponseDto.class)))
                .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        mockMvc.perform(post("/items")
                        .header(USER_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemCreateDto)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @SneakyThrows
    public void updateItem_Success_ReturnUpdatedItem() {

        ItemUpdateDto itemUpdateDto = new ItemUpdateDto("тест вещь", "тест описание", true);
        Long itemId = 3L;
        ItemResponseDto updatedItemResponse = new ItemResponseDto(
                itemId, "новое имя тест вещи", "новое описание", true, new ArrayList<>(), null, null);

        when(restTemplate.patchForObject(
                eq(SERVER_URL + "/items/" + itemId),
                any(HttpEntity.class),
                eq(ItemResponseDto.class)))
                .thenReturn(updatedItemResponse);

        mockMvc.perform(patch("/items/{itemId}", itemId)
                        .header(USER_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemUpdateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("новое имя тест вещи"))
                .andExpect(jsonPath("$.description").value("новое описание"))
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

    }

    @Test
    @SneakyThrows
    public void updateItem_withoutItemId_ThrowException() {
        ItemUpdateDto itemUpdateDto = new ItemUpdateDto("тест вещь", "тест описание", true);

        mockMvc.perform(patch("/items/")
                        .header(USER_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemUpdateDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @SneakyThrows
    public void updateItem_userIdNull_ThrowException() {
        ItemUpdateDto itemUpdateDto = new ItemUpdateDto("тест вещь", "тест описание", true);

        mockMvc.perform(patch("/items/{itemId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemUpdateDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    public void updateItem_internalServerError_ThrowException() {
        ItemUpdateDto itemUpdateDto = new ItemUpdateDto("тест вещь", "тест описание", true);

        when(restTemplate.patchForObject(
                eq(SERVER_URL + "/items/1"),
                any(HttpEntity.class),
                eq(ItemResponseDto.class)))
                .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        mockMvc.perform(patch("/items/{itemId}", 1L)
                        .header(USER_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemUpdateDto)))
                .andExpect(status().isInternalServerError());
    }


    @Test
    @SneakyThrows
    public void findAll_Success_ReturnList() {
        List<ItemResponseDto> items = List.of(
                new ItemResponseDto(1L, "Вещь 1", "Описание 1", true, new ArrayList<>(), null, null),
                new ItemResponseDto(2L, "Вещь 2", "Описание 2", true, new ArrayList<>(), null, null)
        );

        when(restTemplate.getForObject(
                eq(SERVER_URL + "/items?from=0&size=10"),
                eq(ItemResponseDto[].class),
                any(HttpEntity.class)))
                .thenReturn(items.toArray(new ItemResponseDto[0]));

        mockMvc.perform(get("/items")
                        .header(USER_HEADER, userId)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Вещь 1"))
                .andExpect(jsonPath("$[1].name").value("Вещь 2"));
    }

    @Test
    @SneakyThrows
    public void findAll_Pagination_ReturnList() {
        List<ItemResponseDto> items = List.of(
                new ItemResponseDto(6L, "Вещь 6", "Описание 6", true, new ArrayList<>(), null, null),
                new ItemResponseDto(7L, "Вещь 7", "Описание 7", true, new ArrayList<>(), null, null)
        );

        when(restTemplate.getForObject(
                eq(SERVER_URL + "/items?from=5&size=2"),
                eq(ItemResponseDto[].class),
                any(HttpEntity.class)))
                .thenReturn(items.toArray(new ItemResponseDto[0]));

        mockMvc.perform(get("/items")
                        .header(USER_HEADER, userId)
                        .param("from", "5")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(6))
                .andExpect(jsonPath("$[1].id").value(7));
    }

    @Test
    @SneakyThrows
    public void findAll_UserNotFound_ThrowException() {
        when(restTemplate.getForObject(
                any(),
                eq(ItemResponseDto[].class),
                any(HttpEntity.class)))
                .thenThrow(new NotFoundException("Пользователь не найден"));

        mockMvc.perform(get("/items")
                        .header(USER_HEADER, userId)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isNotFound());
    }
    @Test
    @SneakyThrows
    public void findAll_paramMistake_ThrowException() {
        mockMvc.perform(get("/items")
                        .header(USER_HEADER, userId)
                        .param("from", "-1")
                        .param("size", "0"))
                .andExpect(status().isBadRequest());
    }
    @Test
    @SneakyThrows
    public void findAll_ServerError_ThrowException() {
        when(restTemplate.getForObject(
                any(),
                eq(ItemResponseDto[].class),
                any(HttpEntity.class)))
                .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        mockMvc.perform(get("/items")
                        .header(USER_HEADER, userId)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isInternalServerError());
    }


    @Test
    @SneakyThrows
    public void searchItems_validText_success() {
        ItemResponseDto itemResponseDto = createResponseDto();
        itemResponseDto.setName("Тест вещь");
        List<ItemResponseDto> items = List.of(itemResponseDto);

        when(restTemplate.getForObject(
                eq(SERVER_URL + "/items/search?text=текст&from=0&size=10"),
                eq(ItemResponseDto[].class)))
                .thenReturn(items.toArray(new ItemResponseDto[0]));

        mockMvc.perform(get("/items/search")
                        .param("text", "текст")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Тест вещь"));
    }

    @Test
    @SneakyThrows
    public void searchItems_emptyText_returnsEmptyList() {
        when(restTemplate.getForObject(
                eq(SERVER_URL + "/items/search?text=&from=0&size=10"),
                eq(ItemResponseDto[].class)))
                .thenReturn(new ItemResponseDto[0]);

        mockMvc.perform(get("/items/search")
                        .param("text", "")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @SneakyThrows
    public void searchItems_invalidFrom_throwsBadRequest() {
        mockMvc.perform(get("/items/search")
                        .param("text", "текст")
                        .param("from", "-1")
                        .param("size", "10"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    public void searchItems_invalidSize_throwsBadRequest() {
        mockMvc.perform(get("/items/search")
                        .param("text", "текст")
                        .param("from", "0")
                        .param("size", "-1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    public void searchItems_serverNotFound_throwsNotFound() {
        when(restTemplate.getForObject(
                anyString(),
                eq(ItemResponseDto[].class)))
                .thenThrow(new NotFoundException("Сервер не найден"));

        mockMvc.perform(get("/items/search")
                        .param("text", "текст")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isNotFound());
    }

    @Test
    @SneakyThrows
    public void searchItems_internalServerError_throwsServerError() {
        when(restTemplate.getForObject(
                anyString(),
                eq(ItemResponseDto[].class)))
                .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        mockMvc.perform(get("/items/search")
                        .param("text", "тест")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isInternalServerError());
    }

}

