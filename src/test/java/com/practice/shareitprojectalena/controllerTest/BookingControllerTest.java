package com.practice.shareitprojectalena.controllerTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.practice.shareitprojectalena.booking.Booking;
import com.practice.shareitprojectalena.booking.BookingRepository;
import com.practice.shareitprojectalena.booking.dto.BookingCreateDto;
import com.practice.shareitprojectalena.item.Item;
import com.practice.shareitprojectalena.item.ItemRepository;
import com.practice.shareitprojectalena.user.UserRepository;
import com.practice.shareitprojectalena.user.entity.User;
import com.practice.shareitprojectalena.utils.State;
import jakarta.transaction.Transactional;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
    private BookingRepository bookingRepository;
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
    public void create_BookingUnSuccessfulNotFoundException() {
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
    void update_BookingSuccess() {
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
    void update_BookingUnSuccessful_NotFoundException() {
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
                .getContentAsString(StandardCharsets.UTF_8);
        ;
    }

    @Test
    @SneakyThrows
    void update_BookingSuccessReturnResponse() {
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
                .andExpect(jsonPath("$.id").value(bookingId))
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8
                );
    }


    @Test
    @SneakyThrows
    void findById_BookingSuccess() {
        Long bookingId = 1L;
        Long bookerId = 1L;
        Long ownerId = 1L;

        Booking existingBooking = new Booking();
        existingBooking.setItem(new Item());
        existingBooking.getItem().setOwner(new User());
        existingBooking.getItem().getOwner().setId(ownerId);
        existingBooking.setBooker(new User());
        existingBooking.getBooker().setId(bookerId);

        mockMvc.perform(get("/bookings/{bookingId}", bookingId)
                        .header("USER_HEADER", bookerId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingId))
                .andExpect(jsonPath("$.booker.id").value(bookerId))
                .andExpect(jsonPath("$.item.owner.id").value(ownerId))
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

    }


    @Test
    @SneakyThrows
    void update_BookingUnSuccessful_ConflictException() {
        Long bookingId = 1L;
        Long ownerId = 1L;
        boolean approved = true;

        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header("USER_HEADER", ownerId + 1)
                        .param("approved", String.valueOf(approved))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("нет доступа к информации об этом бронировании"))
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
    }

    @Test
    @SneakyThrows
    void getBookingsByState_BookingsFoundSuccess() {
        Long bookerId = 1L;
        State state = State.CURRENT;
        int from = 0;
        int size = 10;

        List<Booking> expected = List.of(new Booking(), new Booking());
        Mockito.when(userRepository.findById(bookerId)).thenReturn(Optional.of(new User()));
        Mockito.when(bookingRepository.findByBookerAndStartBeforeAndEndAfterOrderByStartDesc(any(), any(), any()))
                .thenReturn(expected);

       mockMvc.perform(get("/bookings")
                        .header("USER_HEADER", bookerId)
                        .param("state", state.name())
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id").value(expected.get(0).getId()))
                .andExpect(jsonPath("$.[1].id").value(expected.get(1).getId()))
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

    }
}
