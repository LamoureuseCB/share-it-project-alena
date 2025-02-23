package com.practice.shareitserver.controllerTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.practice.shareitserver.booking.*;
import com.practice.shareitserver.booking.dto.BookingCreateDto;
import com.practice.shareitserver.booking.dto.BookingResponseDto;
import com.practice.shareitserver.error.exceptions.ForbiddenException;
import com.practice.shareitserver.error.exceptions.NotFoundException;
import com.practice.shareitserver.item.Item;
import com.practice.shareitserver.item.ItemMapper;
import com.practice.shareitserver.item.ItemRepository;
import com.practice.shareitserver.item.ItemService;

import com.practice.shareitserver.item.itemDto.ItemCreateDto;
import com.practice.shareitserver.item.itemDto.ItemResponseDto;
import com.practice.shareitserver.user.UserMapper;

import com.practice.shareitserver.user.UserService;
import com.practice.shareitserver.user.dto.UserResponseDto;
import com.practice.shareitserver.user.entity.User;
import com.practice.shareitserver.utils.BookingStatus;
import com.practice.shareitserver.utils.State;
import lombok.SneakyThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;


import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


import static com.practice.shareitserver.utils.RequestConstants.USER_HEADER;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
public class BookingControllerTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    @MockBean
    private UserService userService;

    @MockBean
    private ItemService itemService;

    @SpyBean
    private ItemMapper itemMapper;

    @SpyBean
    private UserMapper userMapper;

    @SpyBean
    private BookingMapper bookingMapper;

    private final Long ownerId = 1L;
    private User owner;
    private User booker;
    private final Long bookerId = 2L;
    private Item item;
    private final Long itemId = 1L;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setId(ownerId);
        owner.setName("Тест владелец");
        owner.setEmail("owner@o.com");

        item = new Item();
        item.setId(itemId);
        item.setName("Тестовая вещь");
        item.setDescription("Описание");
        item.setIsAvailable(true);
        item.setOwner(owner);

        booker = new User();
        booker.setId(bookerId);
        booker.setName("Test Booker");
        booker.setEmail("booker@b.com");

        booker.setItems(List.of(item));
        owner.setItems(List.of(item));

        ItemCreateDto itemCreateDto = new ItemCreateDto();
        itemCreateDto.setName("itemCreateDto");
        itemCreateDto.setDescription("itemCreateDto");
        itemCreateDto.setAvailable(true);
        itemCreateDto.setRequestId(1L);


        when(userService.findById(ownerId)).thenReturn(owner);
        when(userService.findById(bookerId)).thenReturn(booker);
        when(itemService.findById(itemId)).thenReturn(item);
        when(itemMapper.fromCreate(itemCreateDto)).thenReturn(item);
        when(itemService.create(item, ownerId)).thenReturn(item);

    }

    private BookingCreateDto createBookingDto() {
        BookingCreateDto bookingCreateDto = new BookingCreateDto();
        bookingCreateDto.setItemId(item.getId());
        bookingCreateDto.setStart(LocalDateTime.now().plusHours(1));
        bookingCreateDto.setEnd(LocalDateTime.now().plusDays(1));
        return bookingCreateDto;
    }

    private Booking createMockBooking(BookingCreateDto bookingCreateDto) {
        Booking mockBooking = new Booking();
        mockBooking.setId(1L);
        mockBooking.setItem(item);
        mockBooking.setBooker(booker);
        mockBooking.setStart(bookingCreateDto.getStart());
        mockBooking.setEnd(bookingCreateDto.getEnd());
        mockBooking.setStatus(BookingStatus.WAITING);
        return mockBooking;
    }

    private BookingResponseDto createResponseDto(Booking booking) {
        return BookingResponseDto.builder()
                .id(booking.getId())
                .itemId(booking.getItem().getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .status(booking.getStatus())
                .booker(new UserResponseDto(booker.getId(),
                        booker.getName(),
                        booker.getEmail()))
                .item(new ItemResponseDto(
                        booking.getItem().getId(),
                        booking.getItem().getName(),
                        booking.getItem().getDescription(),
                        booking.getItem().getIsAvailable(),
                        new ArrayList<>(),
                        null,
                        null
                ))
                .build();
    }

    @Test
    @SneakyThrows
    void create_ownerBookingOwn_ItemThrowsException() {
        Long bookerId = 1L;
        BookingCreateDto bookingCreateDto = createBookingDto();

        owner.setId(bookerId);


        Item mockItem = new Item();
        mockItem.setId(itemId);
        mockItem.setOwner(owner);

        when(itemService.findById(eq(itemId))).thenReturn(item);
        when(userService.findById(eq(bookerId))).thenReturn(owner);

        when(bookingService.create(any(), eq(bookerId)))
                .thenThrow(new ForbiddenException("Владелец не может забронировать свою вещь"));

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", bookerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingCreateDto)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Владелец не может забронировать свою вещь"));
    }

    @SneakyThrows
    @Test
    void createBooking_WhenValidRequest_ReturnsCreatedBooking() {
        BookingCreateDto bookingCreateDto = createBookingDto();
        Booking mockBooking = createMockBooking(bookingCreateDto);

        when(bookingMapper.fromCreate(bookingCreateDto)).thenReturn(mockBooking);
        when(bookingService.create(any(), eq(bookerId))).thenReturn(mockBooking);
        when(bookingMapper.toResponse(mockBooking)).thenReturn(createResponseDto(mockBooking));

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", bookerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingCreateDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.itemId").value(item.getId()))
                .andExpect(jsonPath("$.booker.id").value(bookerId))
                .andExpect(jsonPath("$.status").value("WAITING"));
    }

    @Test
    @SneakyThrows
    public void create_Booking_ThrowsNotFoundException() {

        Long itemId = 9999L;
        BookingCreateDto bookingCreateDto = new BookingCreateDto();
        bookingCreateDto.setItemId(itemId);
        bookingCreateDto.setStart(LocalDateTime.now().plusHours(1));
        bookingCreateDto.setEnd(LocalDateTime.now().plusDays(1));

        when(itemService.findById(eq(itemId))).thenThrow(new NotFoundException("Предмет с таким ID не найден"));

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", bookerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingCreateDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Предмет с таким ID не найден"));
    }


    @Test
    @SneakyThrows
    public void create_itemNotAvailable_throwsBadRequest() {
        Long itemId = 1L;
        BookingCreateDto bookingCreateDto = new BookingCreateDto();
        bookingCreateDto.setItemId(itemId);
        bookingCreateDto.setStart(LocalDateTime.now().plusHours(1));
        bookingCreateDto.setEnd(LocalDateTime.now().plusDays(1));


        Item item = new Item();
        item.setId(itemId);
        item.setIsAvailable(false);
        item.setOwner(owner);

        when(itemService.findById(eq(itemId))).thenReturn(item);


        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", bookerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingCreateDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Предмет недоступен для бронирования"));
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
    void update_BookingUnSuccessful_NotFoundException() {
        Long bookingId = 9999L;
        boolean approved = true;
        when(bookingService.update(eq(bookingId), eq(ownerId), eq(approved)))
                .thenThrow(new NotFoundException("Бронирование по данному ID не найдено"));

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
    void update_BookingSuccess_ReturnResponse() {
        User booker = new User();
        booker.setId(5L);
        booker.setName("Тестовый пользователь");

        Item mockItem = new Item();
        mockItem.setId(5L);
        mockItem.setOwner(booker);
        mockItem.setIsAvailable(true);
        booker.setItems(List.of(item));

        Booking existingBooking = new Booking();
        existingBooking.setId(5L);
        existingBooking.setItem(mockItem);
        existingBooking.setBooker(booker);
        existingBooking.setStatus(BookingStatus.WAITING);


        when(bookingService.create(existingBooking, booker.getId())).thenReturn(existingBooking);


        Booking updatedBooking = new Booking();
        updatedBooking.setId(existingBooking.getId());
        updatedBooking.setItem(mockItem);
        updatedBooking.setBooker(booker);
        updatedBooking.setStatus(BookingStatus.APPROVED);


        BookingResponseDto expectedResponse = new BookingResponseDto();
        expectedResponse.setId(existingBooking.getId());
        expectedResponse.setStatus(BookingStatus.APPROVED);
        expectedResponse.setBooker(userMapper.toResponse(booker));


        when(bookingService.update(eq(existingBooking.getId()), eq(ownerId), eq(true))).thenReturn(updatedBooking);

        when(bookingMapper.toResponse(updatedBooking))
                .thenReturn(expectedResponse);


        mockMvc.perform(patch("/bookings/{bookingId}", existingBooking.getId())
                        .header(USER_HEADER, ownerId)
                        .param("approved", String.valueOf(true))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(updatedBooking.getId()))
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
    }


    @Test
    @SneakyThrows
    void update_withoutOwner_ThrowsForbiddenEx() {
        Long ownerId = 1000L;
        Long bookingId = 100L;
        boolean approved = true;
        when(bookingService.update(eq(bookingId), eq(ownerId), eq(approved)))
                .thenThrow(new ForbiddenException("Обновить бронирование невозможно"));

        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header(USER_HEADER, ownerId)
                        .param("approved", String.valueOf(approved))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Обновить бронирование невозможно"))
                .andReturn();

    }

    @Test
    @SneakyThrows
    void findById_BookingSuccess() {
        Long bookingId = 100L;
        Long bookerId = 2L;

        Booking existBooking = Booking.builder()
                .id(bookingId)
                .status(BookingStatus.APPROVED)
                .booker(booker)
                .item(item)
                .build();
        BookingResponseDto bookingResponseDto = createResponseDto(existBooking);

        when(bookingService.findById(bookingId)).thenReturn(existBooking);
        when(bookingMapper.toResponse(existBooking)).thenReturn(bookingResponseDto);
        mockMvc.perform(get("/bookings/{bookingId}", bookingId)
                        .header(USER_HEADER, bookerId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingId))
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.booker.id").value(bookerId))
                .andReturn();

    }

    @Test
    @SneakyThrows
    void findById_notOwnerOrBooker_ThrowsConflictEx() {
        Long bookingId = 100L;
        Long bookerId = 1L;
        Long ownerId = 2L;
        Item item1 = new Item();
        User owner2 = new User();
        owner2.setId(ownerId);
        owner2.setItems(List.of(item1));
        item1.setOwner(owner2);

        Booking existBooking = Booking.builder()
                .id(bookingId)
                .status(BookingStatus.APPROVED)
                .booker(booker)
                .item(item1)
                .build();
        when(bookingService.findById(bookingId)).thenReturn(existBooking);

        mockMvc.perform(get("/bookings/{bookingId}", bookingId)
                        .header(USER_HEADER, bookerId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Нет доступа к информации об этом бронировании"));
    }

    @Test
    @SneakyThrows
    void findById_notExistingBooking_ThrowsNotFoundEx() {
        Long bookingId = 9999L;
        when(bookingService.findById(bookingId)).thenThrow(new NotFoundException("Бронирование по данному ID не найдено"));
        mockMvc.perform(get("/bookings/{bookingId}", bookingId)
                        .header(USER_HEADER, bookerId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
    }

    @Test
    @SneakyThrows
    void getBookingsByState_BookingsFoundSuccess() {
        State state = State.CURRENT;
        int from = 0;
        int size = 10;

        Booking mockBooking1 = createMockBooking(createBookingDto());
        mockBooking1.setId(1L);

        Booking mockBooking2 = createMockBooking(createBookingDto());
        mockBooking2.setId(2L);
        BookingResponseDto responseDto1 = createResponseDto(mockBooking1);
        BookingResponseDto responseDto2 = createResponseDto(mockBooking2);


        when(bookingService.getBookingByBooker(eq(state), eq(bookerId), eq(from), eq(size)))
                .thenReturn(new PageImpl<>(List.of(mockBooking1, mockBooking2)));
        when(bookingMapper.toResponse(mockBooking1)).thenReturn(responseDto1);
        when(bookingMapper.toResponse(mockBooking2)).thenReturn(responseDto2);

        mockMvc.perform(get("/bookings")
                        .header(USER_HEADER, bookerId)
                        .param("state", state.name())
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$.length()").value(2));

    }

    @Test
    @SneakyThrows
    void getBookingsByState_returnsEmptyList() {
        State state = State.FUTURE;
        int from = 0;
        int size = 10;
        when(bookingService.getBookingByBooker(eq(state), eq(bookerId), eq(from), eq(size)))
                .thenReturn(new PageImpl<>(List.of()));


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


        Booking mockBooking1 = createMockBooking(createBookingDto());
        mockBooking1.setId(1L);

        Booking mockBooking2 = createMockBooking(createBookingDto());
        mockBooking2.setId(2L);

        BookingResponseDto responseDto1 = createResponseDto(mockBooking1);
        BookingResponseDto responseDto2 = createResponseDto(mockBooking2);

        when(bookingService.getBookingByBooker(eq(state), eq(ownerId), eq(from), eq(size)))
                .thenReturn(new PageImpl<>(List.of(mockBooking1, mockBooking2)));

        when(bookingMapper.toResponse(mockBooking1)).thenReturn(responseDto1);
        when(bookingMapper.toResponse(mockBooking2)).thenReturn(responseDto2);


        mockMvc.perform(get("/bookings/owner")
                        .header(USER_HEADER, ownerId)
                        .param("state", state.name())
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$.length()").value(2));

    }


    @Test
    @SneakyThrows
    void getBookingsByOwner_returnsEmpty() {
        State state = State.FUTURE;
        int from = 0;
        int size = 10;

        when(bookingService.getBookingByBooker(eq(state), eq(ownerId), eq(from), eq(size)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        mockMvc.perform(get("/bookings/owner")
                        .header(USER_HEADER, ownerId)
                        .param("state", state.name())
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @SneakyThrows
    void findAllByOwnerIdAndState_Success() {
        State state = State.CURRENT;
        Long userId = 1L;
        Booking mockBooking1 = createMockBooking(createBookingDto());
        mockBooking1.setId(1L);

        Booking mockBooking2 = createMockBooking(createBookingDto());
        mockBooking2.setId(2L);

        BookingResponseDto responseDto1 = createResponseDto(mockBooking1);
        BookingResponseDto responseDto2 = createResponseDto(mockBooking2);


        when(bookingService.getByStateAndOwner(eq(state), eq(userId), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(mockBooking1, mockBooking2)));


        when(bookingMapper.toResponse(mockBooking1)).thenReturn(responseDto1);
        when(bookingMapper.toResponse(mockBooking2)).thenReturn(responseDto2);

        mockMvc.perform(get("/bookings/owner/{ownerId}", userId)
                        .header(USER_HEADER, userId)
                        .param("state", state.name())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(mockBooking1.getId()))
                .andExpect(jsonPath("$.content[1].id").value(mockBooking2.getId()));
    }


    @Test
    @SneakyThrows
    void findAllByOwnerIdAndState_Fail_DifferentUser() {
        State state = State.CURRENT;
        Long userId = 1L;

        mockMvc.perform(get("/bookings/owner/{ownerId}", 2L)
                        .header(USER_HEADER, userId)
                        .param("state", state.name())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
    }

    @Test
    @SneakyThrows
    void findAllByOwnerIdAndState_ConflictException() {
        State state = State.ALL;
        Long differentUserId = 2L;

        mockMvc.perform(get("/bookings/owner/{ownerId}", ownerId)
                        .header(USER_HEADER, differentUserId)
                        .param("state", state.name())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
    }
}








