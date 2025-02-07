package com.practice.shareitprojectalena.controllerTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.practice.shareitprojectalena.booking.*;
import com.practice.shareitprojectalena.booking.dto.BookingCreateDto;
import com.practice.shareitprojectalena.booking.dto.BookingResponseDto;
import com.practice.shareitprojectalena.error.exceptions.ForbiddenException;
import com.practice.shareitprojectalena.error.exceptions.NotFoundException;
import com.practice.shareitprojectalena.item.Item;
import com.practice.shareitprojectalena.item.ItemMapper;
import com.practice.shareitprojectalena.item.ItemRepository;
import com.practice.shareitprojectalena.item.itemDto.ItemResponseDto;
import com.practice.shareitprojectalena.user.UserMapper;
import com.practice.shareitprojectalena.user.UserRepository;
import com.practice.shareitprojectalena.user.UserService;
import com.practice.shareitprojectalena.user.dto.UserResponseDto;
import com.practice.shareitprojectalena.user.entity.User;
import com.practice.shareitprojectalena.utils.BookingStatus;
import com.practice.shareitprojectalena.utils.State;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.practice.shareitprojectalena.utils.RequestConstants.USER_HEADER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;
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
    private UserRepository userRepository;

    @MockBean
    private BookingRepository bookingRepository;

    @MockBean
    private ItemRepository itemRepository;

    @MockBean
    UserMapper userMapper;

    @MockBean
    BookingMapper bookingMapper;

    @MockBean
    private BookingService bookingService;
    @MockBean
    private UserService userService;

    @MockBean
    private ItemMapper itemMapper;

    private final Long ownerId = 1L;
    private User booker;
    private final Long bookerId = 2L;
    private Item item;
    private final Long itemId = 1L;

    @BeforeEach
    void setUp() {
        User owner = new User();
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


        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
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
        BookingResponseDto mockBookingResponseDto = BookingResponseDto.builder()
                .id(booking.getId())
                .itemId(item.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .status(booking.getStatus())
                .booker(new UserResponseDto(booker.getId(),
                        booker.getName(),
                        booker.getEmail()))
                .item(new ItemResponseDto(
                        item.getId(),
                        item.getName(),
                        item.getDescription(),
                        item.getIsAvailable(),
                        new ArrayList<>(),
                        null,
                        null
                ))
                .build();
        return mockBookingResponseDto;
    }

    @SneakyThrows
    @Test
    void createBooking_WhenValidRequest_ReturnsCreatedBooking() {
        BookingCreateDto bookingCreateDto = createBookingDto();
        Booking mockBooking = createMockBooking(bookingCreateDto);
        BookingResponseDto mockBookingResponseDto = createResponseDto(mockBooking);
        when(userMapper.toResponse(eq(booker)))
                .thenReturn(new UserResponseDto(booker.getId(), booker.getName(), booker.getEmail()));
        when(itemMapper.toResponse(eq(item)))
                .thenReturn(new ItemResponseDto(
                        item.getId(),
                        item.getName(),
                        item.getDescription(),
                        item.getIsAvailable(),
                        new ArrayList<>(),
                        null,
                        null
                ));
        when(bookingMapper.fromCreate(any(BookingCreateDto.class))).thenReturn(mockBooking);
        when(bookingService.create(any(Booking.class), eq(bookerId))).thenReturn(mockBooking);
        when(bookingRepository.save(any(Booking.class))).thenReturn(mockBooking);
        when(bookingMapper.toResponse(eq(mockBooking))).thenReturn(mockBookingResponseDto);

        mockMvc.perform(post("/bookings")
                        .header(USER_HEADER, booker.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingCreateDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.itemId").value(item.getId()))
                .andExpect(jsonPath("$.booker.id").value(booker.getId()))
                .andExpect(jsonPath("$.status").value("WAITING"))
                .andExpect(jsonPath("$.start").value(bookingCreateDto.getStart().format(DateTimeFormatter.ISO_DATE_TIME)))
                .andExpect(jsonPath("$.end").value(bookingCreateDto.getEnd().format(DateTimeFormatter.ISO_DATE_TIME)));
    }

    @Test
    @SneakyThrows
    public void create_BookingUnSuccessfulNotFoundException() {
        Long itemId = 9999L;
        BookingCreateDto bookingCreateDto = createBookingDto();
        bookingCreateDto.setItemId(itemId);

        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

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
    void create_itemNotAvailable_throwsBadRequest() {
        BookingCreateDto bookingCreateDto = createBookingDto();

        when(itemRepository.findById(bookingCreateDto.getItemId())).thenReturn(Optional.empty());
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
    void create_ownerBookingOwnItemThrowsException() {
        Long bookerId = ownerId;
        BookingCreateDto createDto = createBookingDto();

        mockMvc.perform(post("/bookings")
                        .header(USER_HEADER, bookerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Владелец не должен бронировать свою вещь"));
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
        Long bookingId = 100L;
        boolean approved = true;

        Booking mockBooking = Booking.builder()
                .id(bookingId)
                .status(BookingStatus.APPROVED)
                .build();
        BookingResponseDto expectedResponse = new BookingResponseDto();
        expectedResponse.setId(bookingId);
        expectedResponse.setStatus(BookingStatus.APPROVED);

        when(bookingService.update(eq(bookingId), eq(ownerId), eq(approved)))
                .thenReturn(mockBooking);
        when(bookingMapper.toResponse(eq(mockBooking)))
                .thenReturn(expectedResponse);

        MvcResult result = mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header(USER_HEADER, ownerId)
                        .param("approved", String.valueOf(approved))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingId))
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andReturn();


        String responseContent = result.getResponse().getContentAsString();
        BookingResponseDto actualResponse = objectMapper.readValue(responseContent, BookingResponseDto.class);

        assertEquals(expectedResponse.getId(), actualResponse.getId());
        assertEquals(expectedResponse.getStatus(), actualResponse.getStatus());
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
        Long ownerId = userId;
        Booking mockBooking1 = createMockBooking(createBookingDto());
        mockBooking1.setId(1L);

        Booking mockBooking2 = createMockBooking(createBookingDto());
        mockBooking2.setId(2L);

        BookingResponseDto responseDto1 = createResponseDto(mockBooking1);
        BookingResponseDto responseDto2 = createResponseDto(mockBooking2);


        when(bookingService.getByStateAndOwner(eq(state), eq(ownerId), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(mockBooking1, mockBooking2)));


        when(bookingMapper.toResponse(mockBooking1)).thenReturn(responseDto1);
        when(bookingMapper.toResponse(mockBooking2)).thenReturn(responseDto2);

        mockMvc.perform(get("/owner/{ownerId}", ownerId)
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

        mockMvc.perform(get("/owner/{ownerId}", 2L)
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

        mockMvc.perform(get("/owner/{ownerId}", ownerId)
                        .header(USER_HEADER, differentUserId)
                        .param("state", state.name())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
    }
}






