package com.practice.shareitgateway.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.practice.shareitgateway.booking.BookingGatewayController;
import com.practice.shareitgateway.booking.dto.BookingCreateDto;
import com.practice.shareitgateway.booking.dto.BookingResponseDto;
import com.practice.shareitgateway.utils.BookingStatus;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static com.practice.shareitgateway.utils.RequestConstants.SERVER_URL;
import static com.practice.shareitgateway.utils.RequestConstants.USER_HEADER;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingGatewayController.class)
public class BookingGatewayControllerTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;
    @MockBean
    private RestTemplate restTemplate;

    private BookingCreateDto bookingCreateDto;
    private final Long ownerId = 1L;


    @BeforeEach
    void setUp() {
        bookingCreateDto = new BookingCreateDto();
        bookingCreateDto.setItemId(1L);
        bookingCreateDto.setStart(LocalDateTime.now().plusHours(1));
        bookingCreateDto.setEnd(LocalDateTime.now().plusDays(1));
    }

    private BookingResponseDto createBookingResponseDto() {
        BookingResponseDto bookingResponseDto = new BookingResponseDto();
        bookingResponseDto.setItemId(bookingCreateDto.getItemId());
        bookingResponseDto.setStart(bookingCreateDto.getStart());
        bookingResponseDto.setEnd(bookingCreateDto.getEnd());
        return bookingResponseDto;
    }

    @Test
    @SneakyThrows
    void create_Successful() {
        BookingResponseDto bookingResponseDto = createBookingResponseDto();
        when(restTemplate.postForObject(eq(SERVER_URL + "1"), eq(bookingCreateDto), eq(BookingResponseDto.class)))
                .thenReturn(bookingResponseDto);

        mockMvc.perform(post("/bookings")
                        .header(USER_HEADER, ownerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingCreateDto)))
                .andExpect(status().isOk());

    }

    @Test
    @SneakyThrows
    void create_BookingWithInvalidDates_Returns400() {

        bookingCreateDto.setStart(LocalDateTime.now().minusDays(1));
        bookingCreateDto.setEnd(LocalDateTime.now().minusHours(1));
        mockMvc.perform(post("/bookings")
                        .header(USER_HEADER, ownerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingCreateDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void create_BookingWithoutItemId_Returns400() {
        bookingCreateDto.setItemId(null);

        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(USER_HEADER, ownerId)
                        .content(objectMapper.writeValueAsString(bookingCreateDto)))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

    }


    @Test
    @SneakyThrows
    void update_Success_ShouldReturnBookingResponse() {
        long bookingId = 10L;
        boolean approved = true;
        long ownerId = 1L;

        BookingResponseDto expectedResponse = new BookingResponseDto();
        expectedResponse.setId(bookingId);
        expectedResponse.setStatus(BookingStatus.APPROVED);

        when(restTemplate.patchForObject(
                eq(SERVER_URL + "/bookings/" + bookingId + "?approved=" + approved),
                any(HttpEntity.class),
                eq(BookingResponseDto.class)))
                .thenReturn(expectedResponse);

        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header(USER_HEADER, ownerId)
                        .param("approved", String.valueOf(approved))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingId))
                .andExpect(jsonPath("$.status").value(BookingStatus.APPROVED.toString()))
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
    }

    @Test
    @SneakyThrows
    void update_Rejected_ShouldReturnRejectedBooking() {
        long bookingId = 10L;
        boolean approved = false;
        long ownerId = 1L;

        BookingResponseDto expectedResponse = new BookingResponseDto();
        expectedResponse.setId(bookingId);
        expectedResponse.setStatus(BookingStatus.REJECTED);

        when(restTemplate.patchForObject(
                eq(SERVER_URL + "/bookings/" + bookingId + "?approved=" + approved),
                any(HttpEntity.class),
                eq(BookingResponseDto.class)))
                .thenReturn(expectedResponse);

        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header(USER_HEADER, ownerId)
                        .param("approved", String.valueOf(approved))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingId))
                .andExpect(jsonPath("$.status").value(BookingStatus.REJECTED.toString()))
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
    }

    @Test
    @SneakyThrows
    void update_OwnerIdNull_ReturnBadRequest() {
        long bookingId = 10L;
        boolean approved = true;

        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .param("approved", String.valueOf(approved))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
    }

    @Test
    @SneakyThrows
    void update_ServerNotFound_ThrowException() {
        long bookingId = 10L;
        boolean approved = true;
        long ownerId = 1L;

        when(restTemplate.patchForObject(
                eq(SERVER_URL + "/bookings/" + bookingId + "?approved=" + approved),
                any(HttpEntity.class),
                eq(BookingResponseDto.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header(USER_HEADER, ownerId)
                        .param("approved", String.valueOf(approved))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

    }
}


