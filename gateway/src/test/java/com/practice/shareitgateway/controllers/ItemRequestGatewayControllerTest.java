package com.practice.shareitgateway.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.practice.shareitgateway.error.exceptions.NotFoundException;
import com.practice.shareitgateway.request.ItemRequestGatewayController;
import com.practice.shareitgateway.request.dto.ItemRequestCreateDto;
import com.practice.common.dto.ItemRequestFullResponseDto;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
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

import java.time.LocalDateTime;
import java.util.ArrayList;

import static com.practice.shareitgateway.utils.RequestConstants.SERVER_URL;
import static com.practice.shareitgateway.utils.RequestConstants.USER_HEADER;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@WebMvcTest(ItemRequestGatewayController.class)
public class ItemRequestGatewayControllerTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    private RestTemplate restTemplate;

    private final Long userId = 1L;
    private final Long requesterId = 2L;
    private ItemRequestCreateDto createDto;
    private ItemRequestFullResponseDto responseDto;

    @BeforeEach
    void setUp() {
        createDto = new ItemRequestCreateDto("Нужна вещь для аренды");
        responseDto = new ItemRequestFullResponseDto(1L, createDto.getDescription(), requesterId, LocalDateTime.now(),new ArrayList<>());
    }

    @Test
    @SneakyThrows
    public void createItemRequest_success_ReturnCreated() {
        when(restTemplate.postForObject(
                eq(SERVER_URL),
                any(HttpEntity.class),
                eq(ItemRequestFullResponseDto.class)))
                .thenReturn(responseDto);

        mockMvc.perform(post("/requests")
                        .header(USER_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.description").value("Нужна вещь для аренды"));
    }

    @Test
    @SneakyThrows
    public void createItemRequest_invalidData_ThrowsException() {
        createDto.setDescription("");

        mockMvc.perform(post("/requests")
                        .header(USER_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    public void createItemRequest_missUserHeader_ThrowsException() {

        mockMvc.perform(post("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    public void createItemRequest_serverNotFound_ThrowsNotFound() {
        when(restTemplate.postForObject(
                eq(SERVER_URL),
                any(HttpEntity.class),
                eq(ItemRequestFullResponseDto.class)))
                .thenThrow(new NotFoundException("Сервер не найден"));

        mockMvc.perform(post("/requests")
                        .header(USER_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @SneakyThrows
    public void createItemRequest_internalServerError_ThrowsServerError() {
        when(restTemplate.postForObject(
                eq(SERVER_URL),
                any(HttpEntity.class),
                eq(ItemRequestFullResponseDto.class)))
                .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        mockMvc.perform(post("/requests")
                        .header(USER_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isInternalServerError());
    }


    @Test
    @SneakyThrows
    public void getAllRequests_emptyResponse_returnsEmptyList() {
        when(restTemplate.getForObject(
                eq(SERVER_URL + "/requests"),
                eq(ItemRequestFullResponseDto[].class)))
                .thenReturn(new ItemRequestFullResponseDto[0]);

        mockMvc.perform(get("/requests")
                        .header(USER_HEADER, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }


    @Test
    @SneakyThrows
    public void getAllRequestsByPage_successfulResponse_returnsList() {
        ItemRequestFullResponseDto[] responseArray = {
                new ItemRequestFullResponseDto(1L, "Запрос 1", requesterId, LocalDateTime.now(),new ArrayList<>()),
                new ItemRequestFullResponseDto(2L, "Запрос 2", requesterId, LocalDateTime.now(),new ArrayList<>())
        };

        when(restTemplate.getForObject(
                eq(SERVER_URL + "/all?from=0&size=10"),
                eq(ItemRequestFullResponseDto[].class)))
                .thenReturn(responseArray);

        mockMvc.perform(get("/requests/all")
                        .header(USER_HEADER, userId)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].description").value("Запрос 1"))
                .andExpect(jsonPath("$[1].description").value("Запрос 2"));
    }

    @Test
    @SneakyThrows
    public void getAllRequestsByPage_wrongParams_throwsBadRequest() {
        mockMvc.perform(get("/requests/all")
                        .header(USER_HEADER, userId)
                        .param("from", "-1")
                        .param("size", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    public void getAllRequestsByPage_emptyResponse_returnsEmptyList() {
        when(restTemplate.getForObject(
                eq(SERVER_URL + "/all?from=0&size=10"),
                eq(ItemRequestFullResponseDto[].class)))
                .thenReturn(new ItemRequestFullResponseDto[0]);

        mockMvc.perform(get("/requests/all")
                        .header(USER_HEADER, userId)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @SneakyThrows
    public void getAllRequestsByPage_serverError_throwsInternalServerError() {
        when(restTemplate.getForObject(
                anyString(),
                eq(ItemRequestFullResponseDto[].class)))
                .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        mockMvc.perform(get("/requests/all")
                        .header(USER_HEADER, userId)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @SneakyThrows
    public void getAllRequestsByPage_notFound_throwsNotFound() {
        when(restTemplate.getForObject(
                anyString(),
                eq(ItemRequestFullResponseDto[].class)))
                .thenThrow(new NotFoundException(" Сервер не найден"));

        mockMvc.perform(get("/requests/all")
                        .header(USER_HEADER, userId)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isNotFound());
    }

    @Test
    @SneakyThrows
    public void getRequestById_nullRequestId_throwsIllegalArgumentException() {
        mockMvc.perform(get("/requests/null"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    public void getRequestById_validId_returnsItemRequest() {
        Long requestId = 1L;
        ItemRequestFullResponseDto expectedResponse = new ItemRequestFullResponseDto(requestId, "Описание", requesterId, LocalDateTime.now(),new ArrayList<>());

        when(restTemplate.getForObject(
                eq(SERVER_URL + "/requests/" + requestId),
                eq(ItemRequestFullResponseDto.class)))
                .thenReturn(expectedResponse);
        System.out.printf("getRequestById: %s\n", expectedResponse.getId());

        mockMvc.perform(get("/requests/{requestId}", requestId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.description").value(expectedResponse.getDescription()))
                .andExpect(jsonPath("$.requesterId").value(expectedResponse.getRequesterId()));
    }


    @Test
    @SneakyThrows
    public void getAllRequests_successfulResponse_returnsList() {
        LocalDateTime dateTime = LocalDateTime.of(2020, 1, 1, 1, 1);
        ItemRequestFullResponseDto[] response = {
                new ItemRequestFullResponseDto(3L, createDto.getDescription(), 10L, dateTime,new ArrayList<>()),
                new ItemRequestFullResponseDto(4L, createDto.getDescription(), 11L, dateTime,new ArrayList<>())
        };

        when(restTemplate.getForObject(
                eq(SERVER_URL + "/requests"),
                eq(ItemRequestFullResponseDto[].class)))
                .thenReturn(response);

        mockMvc.perform(get("/requests")
                        .header(USER_HEADER, userId))

                .andExpect(jsonPath("$.length()").value(response.length))
                .andExpect(jsonPath("$[0].description").value(createDto.getDescription()))
                .andExpect(jsonPath("$[1].description").value(createDto.getDescription()));
    }

}







