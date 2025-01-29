package com.practice.shareitprojectalena.controllerTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.practice.shareitprojectalena.booking.*;
import com.practice.shareitprojectalena.booking.dto.BookingCreateDto;
import com.practice.shareitprojectalena.booking.dto.BookingResponseDto;
import com.practice.shareitprojectalena.item.Item;
import com.practice.shareitprojectalena.item.ItemMapper;
import com.practice.shareitprojectalena.item.ItemRepository;
import com.practice.shareitprojectalena.item.itemDto.ItemResponseDto;
import com.practice.shareitprojectalena.user.UserMapper;
import com.practice.shareitprojectalena.user.UserRepository;
import com.practice.shareitprojectalena.user.dto.UserResponseDto;
import com.practice.shareitprojectalena.user.entity.User;
import com.practice.shareitprojectalena.utils.BookingStatus;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.practice.shareitprojectalena.utils.RequestConstants.USER_HEADER;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

        Booking mockBooking = new Booking();
        mockBooking.setId(1L);
        mockBooking.setItem(item);
        mockBooking.setBooker(booker);
        mockBooking.setStart(bookingCreateDto.getStart());
        mockBooking.setEnd(bookingCreateDto.getEnd());
        mockBooking.setStatus(BookingStatus.WAITING);

        BookingResponseDto mockBookingResponseDto = BookingResponseDto.builder()
                .id(mockBooking.getId())
                .itemId(item.getId())
                .start(mockBooking.getStart())
                .end(mockBooking.getEnd())
                .status(mockBooking.getStatus())
                .booker(new UserResponseDto(booker.getId(), booker.getName(), booker.getEmail()))
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
        BookingCreateDto bookingCreateDto = new BookingCreateDto();
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
//
//    @Test
//    @SneakyThrows
//    void create_ownerBookingOwnItemThrowsException() {
//        BookingCreateDto bookingCreateDto = new BookingCreateDto();
//        bookingCreateDto.setItemId(itemId);
//        bookingCreateDto.setStart(LocalDateTime.now().plusHours(1));
//        bookingCreateDto.setEnd(LocalDateTime.now().plusDays(1));
//
//        mockMvc.perform(post("/bookings")
//                        .header(USER_HEADER, ownerId)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(bookingCreateDto)))
//                .andExpect(status().isForbidden())
//                .andReturn();
//    }
//
//    @Test
//    @SneakyThrows
//    void create_itemNotAvailable_throwsBadRequest() {
//        Item item = itemRepository.findById(itemId).orElseThrow();
//        item.setIsAvailable(false);
//        itemRepository.save(item);
//        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
//        BookingCreateDto bookingCreateDto = new BookingCreateDto();
//        bookingCreateDto.setItemId(itemId);
//        bookingCreateDto.setStart(LocalDateTime.now().plusHours(1));
//        bookingCreateDto.setEnd(LocalDateTime.now().plusDays(1));
//
//        mockMvc.perform(post("/bookings")
//                        .header(USER_HEADER, bookerId)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(bookingCreateDto)))
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$.message").value("Вещь недоступна"))
//                .andReturn();
//    }
//
//    @Test
//    @SneakyThrows
//    void create_DateIsWrongThrowsBadRequest() {
//        BookingCreateDto bookingCreateDto = new BookingCreateDto();
//        bookingCreateDto.setItemId(itemId);
//        bookingCreateDto.setStart(LocalDateTime.now().minusDays(1));
//        bookingCreateDto.setEnd(LocalDateTime.now().minusHours(1));
//
//        mockMvc.perform(post("/bookings")
//                        .header(USER_HEADER, bookerId)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(bookingCreateDto)))
//                .andExpect(status().isBadRequest())
//                .andReturn();
//    }
//
//    @Test
//    @SneakyThrows
//    void update_BookingSuccess() {
//        BookingCreateDto bookingCreateDto = new BookingCreateDto();
//        bookingCreateDto.setItemId(1L);
//        bookingCreateDto.setStart(LocalDateTime.now().plusHours(1));
//        bookingCreateDto.setEnd(LocalDateTime.now().plusDays(1));
//
//        MvcResult createResult = mockMvc.perform(post("/bookings")
//                        .header(USER_HEADER, bookerId)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(bookingCreateDto)))
//                .andExpect(status().isOk())
//                .andReturn();
//
//        String response = createResult.getResponse().getContentAsString();
//        BookingResponseDto bookingResponse = objectMapper.readValue(response, BookingResponseDto.class);
//        Long bookingId = bookingResponse.getId();
//
//        boolean approved = true;
//        MvcResult updateResult = mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
//                        .header(USER_HEADER, ownerId)
//                        .param("approved", String.valueOf(approved))
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.status").value("APPROVED"))
//                .andExpect(jsonPath("$.id").value(bookingId))
//                .andReturn();
//
//        String updateResponse = updateResult.getResponse().getContentAsString();
//        BookingResponseDto updatedBookingResponse = objectMapper.readValue(updateResponse, BookingResponseDto.class);
//        assertEquals("APPROVED", updatedBookingResponse.getStatus());
//    }
//
//
//    @Test
//    @SneakyThrows
//    void update_BookingUnSuccessful_NotFoundException() {
//        Long bookingId = 9999L;
//        boolean approved = true;
//
//        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
//                        .header(USER_HEADER, ownerId)
//                        .param("approved", String.valueOf(approved))
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isNotFound())
//                .andExpect(jsonPath("$.message").value("Бронирование по данному ID не найдено"))
//                .andReturn()
//                .getResponse()
//                .getContentAsString(StandardCharsets.UTF_8);
//
//    }
//
//    @Test
//    @SneakyThrows
//    void update_BookingSuccessReturnResponse() {
//        boolean approved = true;
//        BookingCreateDto bookingCreateDto = new BookingCreateDto();
//        bookingCreateDto.setItemId(1L);
//        bookingCreateDto.setStart(LocalDateTime.now().plusHours(1));
//        bookingCreateDto.setEnd(LocalDateTime.now().plusDays(1));
//
//        MvcResult createResult = mockMvc.perform(post("/bookings")
//                        .header(USER_HEADER, bookerId)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(bookingCreateDto)))
//                .andExpect(status().isOk())
//                .andReturn();
//
//        String response = createResult.getResponse().getContentAsString();
//        BookingResponseDto bookingResponse = objectMapper.readValue(response, BookingResponseDto.class);
//        Long bookingId = bookingResponse.getId();
//
//        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
//                        .header(USER_HEADER, ownerId)
//                        .param("approved", String.valueOf(approved))
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.id").value(bookingId))
//                .andExpect(jsonPath("$.status").value("APPROVED"))
//                .andReturn()
//                .getResponse()
//                .getContentAsString(StandardCharsets.UTF_8);
//        ;
//    }
//
//    @Test
//    @SneakyThrows
//    void update_withoutOwnerThrowsNotFoundEx() {
//        BookingCreateDto bookingCreateDto = new BookingCreateDto();
//        bookingCreateDto.setItemId(1L);
//        bookingCreateDto.setStart(LocalDateTime.now().plusHours(1));
//        bookingCreateDto.setEnd(LocalDateTime.now().plusDays(1));
//
//        MvcResult createResult = mockMvc.perform(post("/bookings")
//                        .header(USER_HEADER, bookerId)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(bookingCreateDto)))
//                .andExpect(status().isOk())
//                .andReturn();
//        String response = createResult.getResponse().getContentAsString();
//        BookingResponseDto bookingResponse = objectMapper.readValue(response, BookingResponseDto.class);
//        Long bookingId = bookingResponse.getId();
//
//        boolean approved = true;
//        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
//                        .header(USER_HEADER, bookerId)
//                        .param("approved", String.valueOf(approved))
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isNotFound());
//    }
//
//    @Test
//    @SneakyThrows
//    void findById_BookingSuccess() {
//        BookingCreateDto bookingCreateDto = new BookingCreateDto();
//        bookingCreateDto.setItemId(itemId);
//        bookingCreateDto.setStart(LocalDateTime.now().plusHours(1));
//        bookingCreateDto.setEnd(LocalDateTime.now().plusDays(1));
//
//        MvcResult result = mockMvc.perform(post("/bookings")
//                        .header(USER_HEADER, bookerId)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(bookingCreateDto)))
//                .andExpect(status().isOk())
//                .andReturn();
//        String response = result.getResponse().getContentAsString();
//        BookingResponseDto bookingResponse = objectMapper.readValue(response, BookingResponseDto.class);
//        Long bookingId = bookingResponse.getId();
//
//        mockMvc.perform(get("/bookings/{bookingId}", bookingId)
//                        .header(USER_HEADER, bookerId)
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.id").value(bookingId))
//                .andExpect(jsonPath("$.booker.id").value(bookerId))
//                .andReturn()
//                .getResponse()
//                .getContentAsString(StandardCharsets.UTF_8);
//
//    }
//
//    @Test
//    @SneakyThrows
//    void findById_notOwnerOrBookerThrowsConflictEx() {
//        BookingCreateDto bookingCreateDto = new BookingCreateDto();
//        bookingCreateDto.setItemId(itemId);
//        bookingCreateDto.setStart(LocalDateTime.now().plusHours(1));
//        bookingCreateDto.setEnd(LocalDateTime.now().plusDays(1));
//
//        MvcResult result = mockMvc.perform(post("/bookings")
//                        .header(USER_HEADER, bookerId)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(bookingCreateDto)))
//                .andExpect(status().isOk())
//                .andReturn();
//
//        String response = result.getResponse().getContentAsString();
//        BookingResponseDto bookingResponse = objectMapper.readValue(response, BookingResponseDto.class);
//        Long bookingId = bookingResponse.getId();
//
//        User anotherUser3 = new User();
//        anotherUser3.setName("anotherUser3");
//        anotherUser3.setEmail("third@user.com");
//        anotherUser3 = userRepository.save(anotherUser3);
//        Long thirdUserId = anotherUser3.getId();
//
//        mockMvc.perform(get("/bookings/{bookingId}", bookingId)
//                        .header(USER_HEADER, thirdUserId)
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isConflict())
//                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
//    }
//
//    @Test
//    @SneakyThrows
//    void findById_bookingThrowsNotFound() {
//        Long bookingId = 9999L;
//
//        mockMvc.perform(get("/bookings/{bookingId}", bookingId)
//                        .header(USER_HEADER, bookerId)
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isNotFound()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
//    }
//
//
//    @Test
//    @SneakyThrows
//    void getBookingsByState_BookingsFoundSuccess() {
//        State state = State.CURRENT;
//        int from = 0;
//        int size = 10;
//        Item testItem = itemRepository.findById(itemId).orElseThrow();
//        User testBooker = userRepository.findById(bookerId).orElseThrow();
//        Booking booking1 = new Booking();
//        booking1.setItem(testItem);
//        booking1.setBooker(testBooker);
//        booking1.setStart(LocalDateTime.now().minusDays(1));
//        booking1.setEnd(LocalDateTime.now().plusDays(1));
//        bookingRepository.save(booking1);
//
//        Booking booking2 = new Booking();
//        booking2.setItem(testItem);
//        booking2.setBooker(testBooker);
//        booking2.setStart(LocalDateTime.now().minusDays(2));
//        booking2.setEnd(LocalDateTime.now().plusDays(2));
//        bookingRepository.save(booking2);
//
//        mockMvc.perform(get("/bookings")
//                        .header(USER_HEADER, bookerId)
//                        .param("state", state.name())
//                        .param("from", String.valueOf(from))
//                        .param("size", String.valueOf(size))
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$[0].id").exists())
//                .andExpect(jsonPath("$[1].id").exists());
//    }
//
//    @Test
//    @SneakyThrows
//    void getBookingsByState_returnsEmptyList() {
//        State state = State.FUTURE;
//        int from = 0;
//        int size = 10;
//
//        mockMvc.perform(get("/bookings")
//                        .header(USER_HEADER, bookerId)
//                        .param("state", state.name())
//                        .param("from", String.valueOf(from))
//                        .param("size", String.valueOf(size))
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$").isEmpty());
//    }
//
//    @Test
//    @SneakyThrows
//    void getBookingsByOwner_BookingsFoundSuccess() {
//        State state = State.CURRENT;
//        int from = 0;
//        int size = 10;
//        Item testItem = itemRepository.findById(1L).orElseThrow();
//        User testBooker = userRepository.findById(bookerId).orElseThrow();
//
//        Booking booking1 = new Booking();
//        booking1.setItem(testItem);
//        booking1.setBooker(testBooker);
//        booking1.setStart(LocalDateTime.now().minusDays(1));
//        booking1.setEnd(LocalDateTime.now().plusDays(1));
//        bookingRepository.save(booking1);
//
//        Booking booking2 = new Booking();
//        booking2.setItem(testItem);
//        booking2.setBooker(testBooker);
//        booking2.setStart(LocalDateTime.now().minusDays(2));
//        booking2.setEnd(LocalDateTime.now().plusDays(2));
//        bookingRepository.save(booking2);
//
//        mockMvc.perform(get("/bookings/owner")
//                        .header(USER_HEADER, ownerId)
//                        .param("state", state.name())
//                        .param("from", String.valueOf(from))
//                        .param("size", String.valueOf(size))
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$[0].id").exists())
//                .andExpect(jsonPath("$[1].id").exists());
//    }
//
//    @Test
//    @SneakyThrows
//    void getBookingsByOwner_returnsEmpty() {
//        State state = State.FUTURE;
//        int from = 0;
//        int size = 10;
//
//        mockMvc.perform(get("/bookings/owner")
//                        .header(USER_HEADER, ownerId)
//                        .param("state", state.name())
//                        .param("from", String.valueOf(from))
//                        .param("size", String.valueOf(size))
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$").isEmpty());
//
//    }
}


