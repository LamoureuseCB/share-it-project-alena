package com.practice.shareitprojectalena.serviceTest;

import com.practice.shareitprojectalena.booking.Booking;
import com.practice.shareitprojectalena.booking.BookingRepository;
import com.practice.shareitprojectalena.booking.BookingService;
import com.practice.shareitprojectalena.error.exceptions.ForbiddenException;
import com.practice.shareitprojectalena.error.exceptions.NotFoundException;
import com.practice.shareitprojectalena.error.exceptions.ValidationException;
import com.practice.shareitprojectalena.item.Item;
import com.practice.shareitprojectalena.item.ItemRepository;
import com.practice.shareitprojectalena.user.UserRepository;
import com.practice.shareitprojectalena.user.entity.User;
import com.practice.shareitprojectalena.utils.BookingStatus;
import com.practice.shareitprojectalena.utils.State;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
public class BookingServiceTest {
    @InjectMocks
    private BookingService bookingService;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;
    private User owner, booker;
    private Item item;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setId(1L);

        booker = new User();
        booker.setId(2L);

        item = new Item();
        item.setId(1L);
        item.setOwner(owner);
        item.setIsAvailable(true);
    }
    private List<Booking> createBookings() {
        Booking booking1 = new Booking();
        booking1.setId(1L);
        booking1.setStart(LocalDateTime.now().minusDays(1));
        booking1.setEnd(LocalDateTime.now().plusDays(1));

        Booking booking2 = new Booking();
        booking2.setId(2L);
        booking2.setStart(LocalDateTime.now().minusDays(2));
        booking2.setEnd(LocalDateTime.now().plusDays(2));

        return List.of(booking1, booking2);
    }

    @Test
    void create_BookingSuccess() {
        Booking booking = new Booking();
        booking.setItem(item);


        Mockito.when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));
        Mockito.when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        Mockito.when(bookingRepository.save(booking)).thenReturn(booking);

        Booking bookingResult = bookingService.create(booking, booker.getId());
        assertEquals(booking, bookingResult);
    }

    @Test
    void create_ItemNotAvailableThrowValidateException() {
        Booking booking = new Booking();
        booking.setItem(item);
        item.setIsAvailable(false);
        Mockito.when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        Mockito.when((userRepository.findById(booker.getId()))).thenReturn(Optional.of(booker));

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            bookingService.create(booking, booker.getId());
        });

        assertEquals("Предмет недоступен для бронирования", exception.getMessage());
    }

    @Test
    void create_Booking_BookerNotFound_ThrowNotFoundException() {
        Booking booking = new Booking();
        booking.setItem(item);

        Mockito.when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        Mockito.when(userRepository.findById(booker.getId())).thenReturn(Optional.empty());
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            bookingService.create(booking, booker.getId());
        });

        assertEquals("Пользователь по данному ID не найден", exception.getMessage());
    }



    @Test
    void update_BookingSuccess() {
        Long bookingId = 1L;
        boolean approved = true;

        Booking existingBooking = new Booking();
        existingBooking.setItem(new Item());
        existingBooking.getItem().setOwner(owner);

        Mockito.when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(existingBooking));
        Mockito.when(bookingRepository.save(existingBooking)).thenReturn(existingBooking);

        Booking bookingResult = bookingService.update(bookingId, owner.getId(), approved);

        assertEquals(BookingStatus.APPROVED, bookingResult.getStatus());
    }

    @Test
    void update_BookingNotFoundThrowNotFoundException() {
        Long bookingId = 100000L;


        Mockito.when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> bookingService.update(bookingId, owner.getId(), true));

        assertEquals("Бронирование по данному ID не найдено", exception.getMessage());
    }

    @Test
    void update_ThrownForbiddenException_IfUserIsNotOwner() {
        Long bookingId = 1L;
        User owner = new User();
        owner.setId(2L);

        Item item = new Item();
        item.setOwner(owner);

        Booking existingBooking = new Booking();
        existingBooking.setItem(item);

        Mockito.when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(existingBooking));

        Long bookerId = 3L;

        ForbiddenException exception = assertThrows(ForbiddenException.class, () -> bookingService.update(bookingId, bookerId, true));

        assertEquals("Обновить бронирование невозможно", exception.getMessage());
    }



    @Test
    void findAll_ReturnsAllBookings() {
        List<Booking> expected = List.of(new Booking(), new Booking());
        Mockito.when(bookingRepository.findAll()).thenReturn(expected);
        List<Booking> result = bookingService.findAll();
        assertEquals(expected, result);
    }

    @Test
    void deleteById_DeletesBooking() {
        Long bookingId = 1L;
        bookingService.deleteById(bookingId);
        bookingRepository.deleteById(bookingId);
    }

    @Test
    void getByStateAndOwner_ReturnsBookingsByStateAndOwnerId() {
        Long ownerId = 1L;
        Mockito.when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));

        Booking booking1 = new Booking();
        booking1.setEnd(LocalDateTime.now().minusDays(1));
        Booking booking2 = new Booking();
        booking2.setEnd(LocalDateTime.now().minusDays(2));

        List<Booking> expected = List.of(booking1, booking2);

        Page<Booking> expectedPage = new PageImpl<>(expected, PageRequest.of(0, 10), expected.size());

        Mockito.when(bookingRepository.findBookingsByItemOwnerAndEndBeforeOrderByStartDesc(
                        any(User.class), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(expectedPage);

        Page<Booking> result = bookingService.getByStateAndOwner(State.PAST, ownerId, PageRequest.of(0, 10));

        assertNotNull(result);
        assertEquals(expected.size(), result.getTotalElements());
        assertEquals(expected, result.getContent());
    }



    @Test
    void getBookingByBooker() {
        Long bookerId = 1L;
        State state = State.CURRENT;
        int from = 0;
        int size = 10;


        List<Booking> expected = createBookings();

        Mockito.when(userRepository.findById(bookerId)).thenReturn(Optional.of(new User()));

        Pageable pageable = PageRequest.of(from / size, size);
        Page<Booking> expectedPage = new PageImpl<>(expected, pageable, expected.size());

        Mockito.when(bookingRepository.findByBookerAndStartBeforeAndEndAfterOrderByStartDesc(any(), any(), any(), any()))
                .thenReturn(expectedPage);

        Page<Booking> result = bookingService.getBookingByBooker(state, bookerId, from, size);

        assertNotNull(result);
        assertEquals(expected.size(), result.getTotalElements());
        assertEquals(expected, result.getContent());
    }


    @Test
    void create_BookingItem_ThrowNotFoundException() {
        Booking booking = new Booking();
        Long bookerId = 1L;
        Item item = new Item();
        item.setId(1L);
        booking.setItem(item);

        Mockito.when(itemRepository.findById(item.getId())).thenReturn(Optional.empty());
        NotFoundException exception = assertThrows(NotFoundException.class, () -> bookingService.create(booking, bookerId));
        assertEquals("Предмет не найден", exception.getMessage());
    }


    @Test
    void create_BookingIfBookerIsItemOwnerThrowForbiddenException() {
        Long bookerId = 1L;
        User owner = new User();
        owner.setId(bookerId);

        Item item = new Item();
        item.setId(1L);
        item.setOwner(owner);
        Booking booking = new Booking();
        booking.setItem(item);


        Mockito.when(itemRepository.findById(booking.getItem().getId()))
                .thenReturn(Optional.of(item));

        ForbiddenException exception = assertThrows(ForbiddenException.class, () -> bookingService.create(booking, bookerId));

        assertEquals("Владелец не должен бронировать свою вещь", exception.getMessage());

    }


    @Test
    void findById_BookingNotFoundThrowException() {
        Long bookingId = 999999999L;

        Mockito.when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> bookingService.findById(bookingId));

        assertEquals("Бронирование по данному ID не найдено", exception.getMessage());
    }

    @Test
    void findById_BookingFoundSuccess() {
        Long bookingId = 1L;
        Booking expectedBooking = new Booking();
        expectedBooking.setId(bookingId);

        Mockito.when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(expectedBooking));

        Booking result = bookingService.findById(bookingId);

        assertEquals(expectedBooking, result);
    }


    @Test
    void getByStateAndOwner_BookingsFoundSuccess() {
        Long ownerId = 1L;
        State state = State.PAST;

        Booking booking1 = new Booking();
        booking1.setId(1L);
        booking1.setEnd(LocalDateTime.now().minusDays(1));

        Booking booking2 = new Booking();
        booking2.setId(2L);
        booking2.setEnd(LocalDateTime.now().minusDays(2));

        List<Booking> expectedBookings = List.of(booking1, booking2);

        Mockito.when(userRepository.findById(ownerId)).thenReturn(Optional.of(new User()));

        Pageable pageable = PageRequest.of(0, 10);
        Page<Booking> expectedPage = new PageImpl<>(expectedBookings, pageable, expectedBookings.size());

        Mockito.when(bookingRepository.findBookingsByItemOwnerAndEndBeforeOrderByStartDesc(any(), any(), any()))
                .thenReturn(expectedPage);

        Page<Booking> result = bookingService.getByStateAndOwner(state, ownerId, pageable);
        assertNotNull(result);
        assertEquals(expectedBookings.size(), result.getTotalElements());
        assertEquals(expectedBookings, result.getContent());
    }


    @Test
    @SneakyThrows
    void getBookingByBooker_BookingsFoundSuccess() {
        Long bookerId = 1L;
        State state = State.FUTURE;
        int from = 0;
        int size = 10;

        Booking booking1 = new Booking();
        booking1.setId(1L);
        booking1.setStart(LocalDateTime.now().plusDays(1));
        booking1.setEnd(LocalDateTime.now().plusDays(2));

        Booking booking2 = new Booking();
        booking2.setId(2L);
        booking2.setStart(LocalDateTime.now().plusDays(3));
        booking2.setEnd(LocalDateTime.now().plusDays(4));

        List<Booking> expectedBookings = List.of(booking1, booking2);

        User user = new User();
        user.setId(bookerId);

        Mockito.when(userRepository.findById(bookerId)).thenReturn(Optional.of(user));

        Pageable pageable = PageRequest.of(from / size, size);
        Page<Booking> expectedPage = new PageImpl<>(expectedBookings, pageable, expectedBookings.size());

        Mockito.when(bookingRepository.findByBookerAndStartAfterOrderByStartDesc(eq(user), any(), eq(pageable)))
                .thenReturn(expectedPage);

        Page<Booking> result = bookingService.getBookingByBooker(state, bookerId, from, size);

        assertNotNull(result);
        assertEquals(expectedBookings.size(), result.getTotalElements());
        assertIterableEquals(expectedBookings, result.getContent());
    }

    @Test
    void getBookingByBooker_FutureBookingsFoundSuccess() {
        Long bookerId = 1L;
        State state = State.FUTURE;


        Booking booking1 = new Booking();
        booking1.setId(1L);
        booking1.setStart(LocalDateTime.now().plusDays(1));
        booking1.setEnd(LocalDateTime.now().plusDays(2));

        Booking booking2 = new Booking();
        booking2.setId(2L);
        booking2.setStart(LocalDateTime.now().plusDays(3));
        booking2.setEnd(LocalDateTime.now().plusDays(4));
        List<Booking> expectedBookings = List.of(booking1, booking2);
        User user = new User();
        user.setId(bookerId);
        user.setName("User1");


        Mockito.when(userRepository.findById(bookerId)).thenReturn(Optional.of(user));

        Pageable pageable = PageRequest.of(0, 10);
        Page<Booking> expectedPage = new PageImpl<>(expectedBookings, pageable, expectedBookings.size());
        Mockito.when(bookingRepository.findByBookerAndStartAfterOrderByStartDesc(eq(user), any(), eq(pageable)))
                .thenReturn(expectedPage);

        Page<Booking> result = bookingService.getBookingByBooker(state, bookerId, 0, 10);
        assertNotNull(result);
        assertEquals(expectedBookings.size(), result.getTotalElements());
        assertEquals(expectedBookings, result.getContent());
    }


    @Test
    void getBookingByBooker_WaitingBookingsFoundSuccess() {
        Long bookerId = 1L;
        State state = State.WAITING;

        Booking booking1 = new Booking();
        booking1.setId(1L);
        booking1.setStatus(BookingStatus.WAITING);

        Booking booking2 = new Booking();
        booking2.setId(2L);
        booking2.setStatus(BookingStatus.WAITING);

        List<Booking> expectedBookings = List.of(booking1, booking2);
        User user = new User();
        user.setId(bookerId);
        user.setName("User1");

        Mockito.when(userRepository.findById(bookerId)).thenReturn(Optional.of(user));

        Pageable pageable = PageRequest.of(0, 10);
        Page<Booking> expectedPage = new PageImpl<>(expectedBookings, pageable, expectedBookings.size());

        Mockito.when(bookingRepository.findBookingsByItemOwnerAndStatusOrderByStartDesc(eq(user), eq(BookingStatus.WAITING), eq(pageable)))
                .thenReturn(expectedPage);

        Page<Booking> result = bookingService.getBookingByBooker(state, bookerId, 0, 10);

        assertNotNull(result);
        assertEquals(expectedBookings.size(), result.getTotalElements());
        assertEquals(expectedBookings, result.getContent());
    }

    @Test
    void getBookingByBooker_RejectedBookingsFoundSuccess() {
        Long bookerId = 1L;
        State state = State.REJECTED;

        Booking booking1 = new Booking();
        booking1.setId(1L);
        booking1.setStatus(BookingStatus.REJECTED);

        Booking booking2 = new Booking();
        booking2.setId(2L);
        booking2.setStatus(BookingStatus.REJECTED);

        List<Booking> expectedBookings = List.of(booking1, booking2);
        User user = new User();
        user.setId(bookerId);
        user.setName("User1");

        Mockito.when(userRepository.findById(bookerId)).thenReturn(Optional.of(user));

        Pageable pageable = PageRequest.of(0, 10);
        Page<Booking> expectedPage = new PageImpl<>(expectedBookings, pageable, expectedBookings.size());

        Mockito.when(bookingRepository.findBookingsByItemOwnerAndStatusOrderByStartDesc(eq(user), eq(BookingStatus.REJECTED), eq(pageable)))
                .thenReturn(expectedPage);

        Page<Booking> result = bookingService.getBookingByBooker(state, bookerId, 0, 10);

        assertNotNull(result);
        assertEquals(expectedBookings.size(), result.getTotalElements());
        assertEquals(expectedBookings, result.getContent());
    }

    @Test
    void getBookingByBooker_DefaultBookingsFoundSuccess() {
        Long bookerId = 1L;
        State state = State.ALL;

        Booking booking1 = new Booking();
        booking1.setId(1L);

        Booking booking2 = new Booking();
        booking2.setId(2L);

        List<Booking> expectedBookings = List.of(booking1, booking2);
        User user = new User();
        user.setId(bookerId);
        user.setName("User1");

        Mockito.when(userRepository.findById(bookerId)).thenReturn(Optional.of(user));

        Pageable pageable = PageRequest.of(0, 10);
        Page<Booking> expectedPage = new PageImpl<>(expectedBookings, pageable, expectedBookings.size());

        Mockito.when(bookingRepository.findByBookerOrderByStartDesc(eq(user), eq(pageable)))
                .thenReturn(expectedPage);

        Page<Booking> result = bookingService.getBookingByBooker(state, bookerId, 0, 10);

        assertNotNull(result);
        assertEquals(expectedBookings.size(), result.getTotalElements());
        assertEquals(expectedBookings, result.getContent());
    }


}

