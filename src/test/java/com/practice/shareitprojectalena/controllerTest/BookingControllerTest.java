package com.practice.shareitprojectalena.controllerTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.practice.shareitprojectalena.booking.Booking;
import com.practice.shareitprojectalena.booking.dto.BookingCreateDto;
import com.practice.shareitprojectalena.item.Item;
import com.practice.shareitprojectalena.item.ItemRepository;
import com.practice.shareitprojectalena.user.UserRepository;
import com.practice.shareitprojectalena.user.entity.User;
import jakarta.transaction.Transactional;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class BookingControllerTest {
    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRepository itemRepository;
    Long ownerId = 1L;
    Long itemId = 1L;

    @BeforeEach
    public void setUp() {

        User owner = new User();
        owner.setId(ownerId);
        userRepository.save(owner);

        Item item = new Item();
        item.setId(itemId);
        item.setIsAvailable(true);
        item.setOwner(owner);
        itemRepository.save(item);
    }

    @Test
    @SneakyThrows
    public void testCreateBookingSuccess() {
        Long bookerId = 1L;
        BookingCreateDto bookingCreateDto = new BookingCreateDto();
        bookingCreateDto.setItemId(itemId);

        mockMvc.perform(post("/bookings")
                        .header("USER_HEADER", bookerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingCreateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status").value("WAITING"))
                .andExpect(jsonPath("$.booker.id").value(bookerId))
                .andExpect(jsonPath("$.item.id").value(itemId))
                .andExpect(jsonPath("$.item.owner.id").value(ownerId))
                .andExpect(jsonPath("$.item.isAvailable").value(true));
    }

    @Test
    @SneakyThrows
    public void testCreateBookingUnSuccessfulNotFoundException() {
        Long bookerId = 1L;
        Long itemId = 2L;
        BookingCreateDto bookingCreateDto = new BookingCreateDto();
        bookingCreateDto.setItemId(itemId);

        mockMvc.perform(post("/bookings")
                        .header("USER_HEADER", bookerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingCreateDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Объект не найден"))
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
    }

    @Test
    @SneakyThrows
    public void testUpdateBookingSuccess() {
        Long bookingId = 1L;
        Long ownerId = 1L;
        boolean approved = true;

        Booking existingBooking = new Booking();
        existingBooking.setItem(new Item());
        existingBooking.getItem().setOwner(new User());
        existingBooking.getItem().getOwner().setId(ownerId);


        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header("USER_HEADER", ownerId)
                        .param("approved", String.valueOf(approved))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
    }

    @Test
    @SneakyThrows
    public void testUpdateBookingUnSuccessful_NotFoundException() {
        Long bookingId = 1L;
        Long ownerId = 1L;
        boolean approved = true;

        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header("USER_HEADER", ownerId)
                        .param("approved", String.valueOf(approved))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Объект не найден"))
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);;
    }

}
