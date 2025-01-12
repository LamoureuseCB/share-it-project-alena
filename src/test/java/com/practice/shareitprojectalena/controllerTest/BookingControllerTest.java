package com.practice.shareitprojectalena.controllerTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.practice.shareitprojectalena.booking.Booking;
import com.practice.shareitprojectalena.booking.BookingRepository;
import com.practice.shareitprojectalena.booking.dto.BookingCreateDto;
import com.practice.shareitprojectalena.booking.dto.BookingResponseDto;
import com.practice.shareitprojectalena.item.Item;
import com.practice.shareitprojectalena.item.ItemRepository;
import com.practice.shareitprojectalena.user.UserMapper;
import com.practice.shareitprojectalena.user.UserRepository;
import com.practice.shareitprojectalena.user.entity.User;
import com.practice.shareitprojectalena.utils.BookingStatus;
import com.practice.shareitprojectalena.utils.State;
import jakarta.transaction.Transactional;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.practice.shareitprojectalena.utils.RequestConstants.USER_HEADER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private BookingRepository bookingRepository;

    @MockBean
    private ItemRepository itemRepository;
    @Spy
    UserMapper userMapper;

    private User owner;
    private Long ownerId = 1L;
    private User booker;
    private Long bookerId = 2L;
    private Item item;
    private Long itemId = 1L;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setId(ownerId);
        owner.setName("Тест владелец");
        owner.setEmail("owner@o.com");


        booker = new User();
        booker.setId(bookerId);
        booker.setName("Test Booker");
        booker.setEmail("booker@b.com");


        item = new Item();
        item.setId(itemId);
        item.setName("Тестовая вещь");
        item.setDescription("Описание");
        item.setIsAvailable(true);
        item.setOwner(owner);

        owner.setItems(List.of(item));
        booker.setItems(List.of(item));

        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
    }

    @SneakyThrows
    @Test
    void createBooking_WhenValidRequest_ReturnsCreatedBooking() {
        BookingCreateDto bookingCreateDto = new BookingCreateDto();
        bookingCreateDto.setItemId(item.getId());
        bookingCreateDto.setStart(LocalDateTime.now().plusHours(1));
        bookingCreateDto.setEnd(LocalDateTime.now().plusDays(1));

        BookingResponseDto mockBookingResponseDto = BookingResponseDto.builder()
                .id(1L)
                .itemId(item.getId())
                .start(bookingCreateDto.getStart())
                .end(bookingCreateDto.getEnd())
                .status(BookingStatus.WAITING)
                .booker(userMapper.toResponse(booker))
                .build();

        when(bookingRepository.save(any())).thenReturn(mockBookingResponseDto);

        mockMvc.perform(post("/bookings")
                        .header(USER_HEADER, booker.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingCreateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.itemId").value(item.getId()))
                .andExpect(jsonPath("$.booker.id").value(booker.getId()))
                .andExpect(jsonPath("$.status").value("WAITING"))
                .andExpect(jsonPath("$.start").value(bookingCreateDto.getStart().toString()))
                .andExpect(jsonPath("$.end").value(bookingCreateDto.getEnd().toString()))
                .andReturn();
    }

    @Test
    @SneakyThrows
    public void create_BookingUnSuccessfulNotFoundException() {
        Long itemId = 9999L;
        BookingCreateDto bookingCreateDto = new BookingCreateDto();
        bookingCreateDto.setItemId(itemId);

        mockMvc.perform(post("/bookings")
                        .header(USER_HEADER, bookerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingCreateDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Предмет не найден"))
                .andReturn();
    }

    @Test
    @SneakyThrows
    void create_ownerBookingOwnItemThrowsException() {
        BookingCreateDto bookingCreateDto = new BookingCreateDto();
        bookingCreateDto.setItemId(itemId);
        bookingCreateDto.setStart(LocalDateTime.now().plusHours(1));
        bookingCreateDto.setEnd(LocalDateTime.now().plusDays(1));

        mockMvc.perform(post("/bookings")
                        .header(USER_HEADER, ownerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingCreateDto)))
                .andExpect(status().isForbidden())
                .andReturn();
    }

    @Test
    @SneakyThrows
    void create_itemNotAvailable_throwsBadRequest() {
        Item item = itemRepository.findById(itemId).orElseThrow();
        item.setIsAvailable(false);
        itemRepository.save(item);

        BookingCreateDto bookingCreateDto = new BookingCreateDto();
        bookingCreateDto.setItemId(itemId);
        bookingCreateDto.setStart(LocalDateTime.now().plusHours(1));
        bookingCreateDto.setEnd(LocalDateTime.now().plusDays(1));

        mockMvc.perform(post("/bookings")
                        .header(USER_HEADER, bookerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingCreateDto)))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    @SneakyThrows
    void create_DateIsWrongThrowsBadRequest() {
        BookingCreateDto bookingCreateDto = new BookingCreateDto();
        bookingCreateDto.setItemId(itemId);
        bookingCreateDto.setStart(LocalDateTime.now().minusDays(1));
        bookingCreateDto.setEnd(LocalDateTime.now().minusHours(1));

        mockMvc.perform(post("/bookings")
                        .header(USER_HEADER, bookerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingCreateDto)))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    @SneakyThrows
    void update_BookingSuccess() {
        BookingCreateDto bookingCreateDto = new BookingCreateDto();
        bookingCreateDto.setItemId(1L);
        bookingCreateDto.setStart(LocalDateTime.now().plusHours(1));
        bookingCreateDto.setEnd(LocalDateTime.now().plusDays(1));

        MvcResult createResult = mockMvc.perform(post("/bookings")
                        .header(USER_HEADER, bookerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingCreateDto)))
                .andExpect(status().isOk())
                .andReturn();

        String response = createResult.getResponse().getContentAsString();
        BookingResponseDto bookingResponse = objectMapper.readValue(response, BookingResponseDto.class);
        Long bookingId = bookingResponse.getId();

        boolean approved = true;
        MvcResult updateResult = mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header(USER_HEADER, ownerId)
                        .param("approved", String.valueOf(approved))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andReturn();

        String updateResponse = updateResult.getResponse().getContentAsString();
        BookingResponseDto updatedBookingResponse = objectMapper.readValue(updateResponse, BookingResponseDto.class);
        assertEquals("APPROVED", updatedBookingResponse.getStatus());
    }


    @Test
    @SneakyThrows
    void update_BookingUnSuccessful_NotFoundException() {
        Long bookingId = 9999L;
        boolean approved = true;

        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header(USER_HEADER, ownerId)
                        .param("approved", String.valueOf(approved))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Бронирование по данному ID не найдено"))
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

    }

    @Test
    @SneakyThrows
    void update_BookingSuccessReturnResponse() {
        boolean approved = true;
        BookingCreateDto bookingCreateDto = new BookingCreateDto();
        bookingCreateDto.setItemId(1L);
        bookingCreateDto.setStart(LocalDateTime.now().plusHours(1));
        bookingCreateDto.setEnd(LocalDateTime.now().plusDays(1));

        MvcResult createResult = mockMvc.perform(post("/bookings")
                        .header(USER_HEADER, bookerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingCreateDto)))
                .andExpect(status().isOk())
                .andReturn();

        String response = createResult.getResponse().getContentAsString();
        BookingResponseDto bookingResponse = objectMapper.readValue(response, BookingResponseDto.class);
        Long bookingId = bookingResponse.getId();

        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header(USER_HEADER, ownerId)
                        .param("approved", String.valueOf(approved))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingId))
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        ;
    }

    @Test
    @SneakyThrows
    void update_withoutOwnerThrowsNotFoundEx() {
        BookingCreateDto bookingCreateDto = new BookingCreateDto();
        bookingCreateDto.setItemId(1L);
        bookingCreateDto.setStart(LocalDateTime.now().plusHours(1));
        bookingCreateDto.setEnd(LocalDateTime.now().plusDays(1));

        MvcResult createResult = mockMvc.perform(post("/bookings")
                        .header(USER_HEADER, bookerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingCreateDto)))
                .andExpect(status().isOk())
                .andReturn();
        String response = createResult.getResponse().getContentAsString();
        BookingResponseDto bookingResponse = objectMapper.readValue(response, BookingResponseDto.class);
        Long bookingId = bookingResponse.getId();

        boolean approved = true;
        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header(USER_HEADER, bookerId)
                        .param("approved", String.valueOf(approved))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @SneakyThrows
    void findById_BookingSuccess() {
        BookingCreateDto bookingCreateDto = new BookingCreateDto();
        bookingCreateDto.setItemId(itemId);
        bookingCreateDto.setStart(LocalDateTime.now().plusHours(1));
        bookingCreateDto.setEnd(LocalDateTime.now().plusDays(1));

        MvcResult result = mockMvc.perform(post("/bookings")
                        .header(USER_HEADER, bookerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingCreateDto)))
                .andExpect(status().isOk())
                .andReturn();
        String response = result.getResponse().getContentAsString();
        BookingResponseDto bookingResponse = objectMapper.readValue(response, BookingResponseDto.class);
        Long bookingId = bookingResponse.getId();

        mockMvc.perform(get("/bookings/{bookingId}", bookingId)
                        .header(USER_HEADER, bookerId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingId))
                .andExpect(jsonPath("$.booker.id").value(bookerId))
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

    }

    @Test
    @SneakyThrows
    void findById_notOwnerOrBookerThrowsConflictEx() {
        BookingCreateDto bookingCreateDto = new BookingCreateDto();
        bookingCreateDto.setItemId(itemId);
        bookingCreateDto.setStart(LocalDateTime.now().plusHours(1));
        bookingCreateDto.setEnd(LocalDateTime.now().plusDays(1));

        MvcResult result = mockMvc.perform(post("/bookings")
                        .header(USER_HEADER, bookerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingCreateDto)))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        BookingResponseDto bookingResponse = objectMapper.readValue(response, BookingResponseDto.class);
        Long bookingId = bookingResponse.getId();

        User anotherUser3 = new User();
        anotherUser3.setName("anotherUser3");
        anotherUser3.setEmail("third@user.com");
        anotherUser3 = userRepository.save(anotherUser3);
        Long thirdUserId = anotherUser3.getId();

        mockMvc.perform(get("/bookings/{bookingId}", bookingId)
                        .header(USER_HEADER, thirdUserId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
    }

    @Test
    @SneakyThrows
    void findById_bookingThrowsNotFound() {
        Long bookingId = 9999L;

        mockMvc.perform(get("/bookings/{bookingId}", bookingId)
                        .header(USER_HEADER, bookerId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
    }


    @Test
    @SneakyThrows
    void getBookingsByState_BookingsFoundSuccess() {
        State state = State.CURRENT;
        int from = 0;
        int size = 10;
        Item testItem = itemRepository.findById(itemId).orElseThrow();
        User testBooker = userRepository.findById(bookerId).orElseThrow();
        Booking booking1 = new Booking();
        booking1.setItem(testItem);
        booking1.setBooker(testBooker);
        booking1.setStart(LocalDateTime.now().minusDays(1));
        booking1.setEnd(LocalDateTime.now().plusDays(1));
        bookingRepository.save(booking1);

        Booking booking2 = new Booking();
        booking2.setItem(testItem);
        booking2.setBooker(testBooker);
        booking2.setStart(LocalDateTime.now().minusDays(2));
        booking2.setEnd(LocalDateTime.now().plusDays(2));
        bookingRepository.save(booking2);

        mockMvc.perform(get("/bookings")
                        .header(USER_HEADER, bookerId)
                        .param("state", state.name())
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[1].id").exists());
    }

    @Test
    @SneakyThrows
    void getBookingsByState_returnsEmptyList() {
        State state = State.FUTURE;
        int from = 0;
        int size = 10;

        mockMvc.perform(get("/bookings")
                        .header(USER_HEADER, bookerId)
                        .param("state", state.name())
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @SneakyThrows
    void getBookingsByOwner_BookingsFoundSuccess() {
        State state = State.CURRENT;
        int from = 0;
        int size = 10;
        Item testItem = itemRepository.findById(1L).orElseThrow();
        User testBooker = userRepository.findById(bookerId).orElseThrow();

        Booking booking1 = new Booking();
        booking1.setItem(testItem);
        booking1.setBooker(testBooker);
        booking1.setStart(LocalDateTime.now().minusDays(1));
        booking1.setEnd(LocalDateTime.now().plusDays(1));
        bookingRepository.save(booking1);

        Booking booking2 = new Booking();
        booking2.setItem(testItem);
        booking2.setBooker(testBooker);
        booking2.setStart(LocalDateTime.now().minusDays(2));
        booking2.setEnd(LocalDateTime.now().plusDays(2));
        bookingRepository.save(booking2);

        mockMvc.perform(get("/bookings/owner")
                        .header(USER_HEADER, ownerId)
                        .param("state", state.name())
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[1].id").exists());
    }

    @Test
    @SneakyThrows
    void getBookingsByOwner_returnsEmpty() {
        State state = State.FUTURE;
        int from = 0;
        int size = 10;

        mockMvc.perform(get("/bookings/owner")
                        .header(USER_HEADER, ownerId)
                        .param("state", state.name())
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

    }
}


