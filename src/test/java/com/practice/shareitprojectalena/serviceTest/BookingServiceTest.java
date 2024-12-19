package com.practice.shareitprojectalena.serviceTest;

import com.practice.shareitprojectalena.booking.Booking;
import com.practice.shareitprojectalena.booking.BookingRepository;
import com.practice.shareitprojectalena.booking.BookingService;
import com.practice.shareitprojectalena.error.exceptions.ConflictException;
import com.practice.shareitprojectalena.error.exceptions.ForbiddenException;
import com.practice.shareitprojectalena.error.exceptions.NotFoundException;
import com.practice.shareitprojectalena.item.Item;
import com.practice.shareitprojectalena.item.ItemRepository;
import com.practice.shareitprojectalena.user.UserRepository;
import com.practice.shareitprojectalena.user.entity.User;
import com.practice.shareitprojectalena.utils.BookingStatus;
import com.practice.shareitprojectalena.utils.State;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;

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

    @Test
    void create_BookingSuccess() {
        Booking booking = new Booking();
        Long bookerId = 1L;
        Item item = new Item();
        item.setIsAvailable(true);

        Mockito.when(userRepository.findById(bookerId)).thenReturn(Optional.of(new User()));
        Mockito.when(bookingRepository.save(booking)).thenReturn(booking);

        Booking bookingResult = bookingService.create(booking, bookerId);
        assertEquals(booking, bookingResult);
    }

    @Test
    void create_ItemNotAvailableThrowConflictException() {
        Booking booking = new Booking();
        Long bookerId = 1L;
        Item item = new Item();
        item.setIsAvailable(false);

        ConflictException exception = assertThrows(ConflictException.class, () -> {
            bookingService.create(booking, bookerId);
        });

        assertEquals(item.getName() + " недоступен для бронирования", exception.getMessage());
    }

    @Test
    void create_BookingBookerNotFoundThrowNotFoundException() {
        Booking booking = new Booking();
        Long bookerId = 100000L;
        Item item = new Item();
        item.setIsAvailable(true);

        Mockito.when(userRepository.findById(bookerId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            bookingService.create(booking, bookerId);
        });

        assertEquals("Пользователь по данному ID не найден", exception.getMessage());
    }

    @Test
    void update_BookingSuccess() {
        Long bookingId = 1L;
        Long userId = 1L;
        boolean approved = true;

        Booking existingBooking = new Booking();
        existingBooking.setItem(new Item());
        existingBooking.getItem().setOwner(new User());
        existingBooking.getItem().getOwner().setId(userId);

        Mockito.when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(existingBooking));

        Booking bookingResult = bookingService.update(bookingId, userId, approved);

        assertEquals(BookingStatus.APPROVED, bookingResult.getStatus());
    }

    @Test
    void update_BookingNotFoundThrowNotFoundException() {
        Long bookingId = 100000L;
        Long userId = 1L;
        boolean approved = true;

        Mockito.when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            bookingService.update(bookingId, userId, approved);
        });

        assertEquals("Бронирование по данному ID не найдено", exception.getMessage());
    }

    @Test
    void update_BookingOwnerNotFoundThrowNotFoundException() {
        Long bookingId = 1L;
        Long userId = 100000L;
        boolean approved = true;

        Booking existingBooking = new Booking();
        existingBooking.setItem(new Item());
        existingBooking.getItem().setOwner(new User());
        existingBooking.getItem().getOwner().setId(userId);

        Mockito.when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(existingBooking));

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            bookingService.update(bookingId, userId, approved);
        });

        assertEquals("Обновить бронирование невозможно", exception.getMessage());
    }

    @Test
    void update_ThrownForbiddException_IfUserIsNotOwner() {
        Long bookingId = 1L;
        Long userId = 1L;
        boolean approved = true;

        Booking existingBooking = new Booking();
        existingBooking.setItem(new Item());
        existingBooking.getItem().setOwner(new User());
        existingBooking.getItem().getOwner().setId(2L);

        Mockito.when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(existingBooking));

        ForbiddenException exception = assertThrows(ForbiddenException.class, () -> {
            bookingService.update(bookingId, userId, approved);
        });

        assertEquals("Обновить бронирование невозможно", exception.getMessage());
    }

    @Test
    void update_BookingStatusRejectedThrowForbiddenException() {
        Long bookingId = 1L;
        Long userId = 1L;
        boolean approved = false;

        Booking existingBooking = new Booking();
        existingBooking.setItem(new Item());
        existingBooking.getItem().setOwner(new User());
        existingBooking.getItem().getOwner().setId(userId);
        existingBooking.setStatus(BookingStatus.REJECTED);

        Mockito.when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(existingBooking));

        ForbiddenException exception = assertThrows(ForbiddenException.class, () -> {
            bookingService.update(bookingId, userId, approved);
        });

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
        User owner = new User();
        owner.setId(1L);
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(owner));

        List<Booking> expected = List.of(new Booking(), new Booking());
        Mockito.when(bookingRepository.findBookingsByItemOwnerAndStartAfterOrderByStartDesc(any(), any())).thenReturn(expected);

        List<Booking> result = bookingService.getByStateAndOwner(State.FUTURE, 1L);
        assertEquals(expected, result);
    }


    @Test
    void getBookingByBooker() {
        Long bookerId = 1L;
        State state = State.CURRENT;
        int from = 0;
        int size = 10;

        List<Booking> expected = List.of(new Booking(), new Booking());
        Mockito.when(userRepository.findById(bookerId)).thenReturn(Optional.of(new User()));
        Mockito.when(bookingRepository.findByBookerAndStartBeforeAndEndAfterOrderByStartDesc(any(), any(), any()))
                .thenReturn(expected);

        List<Booking> result = bookingService.getBookingByBooker(state, bookerId, from, size);
        assertEquals(expected, result);
    }

    @Test
    void create_BookingItemNotAvailableThrowConflictException() {
        Booking booking = new Booking();
        Long bookerId = 1L;
        Item item = new Item();
        item.setIsAvailable(false);

        ConflictException exception = assertThrows(ConflictException.class, () -> {
            bookingService.create(booking, bookerId);
        });

        assertEquals("Предмет недоступен для бронирования", exception.getMessage());
    }

    @Test
    void create_BookingItemNotFoundThrowNotFoundException() {
        Booking booking = new Booking();
        Long bookerId = 1L;

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            bookingService.create(booking, bookerId);
        });

        assertEquals("Предмет не найден", exception.getMessage());


    }

    @Test
    void create_BookingIfBookerNotFoundThrowException() {
        Booking booking = new Booking();
        Long bookerId = 1111111111L;

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            bookingService.create(booking, bookerId);
        });

        assertEquals("Пользователь по данному ID не найден", exception.getMessage());

    }

    @Test
    void create_BookingIfBookerIsItemOwnerThrowForbiddenException() {
        Booking booking = new Booking();
        Long bookerId = 1L;
        Item item = new Item();
        item.setOwner(new User());
        item.getOwner().setId(bookerId);

        Mockito.when(itemRepository.findById(booking.getItem().getId()))
                .thenReturn(Optional.of(item));

        ForbiddenException exception = assertThrows(ForbiddenException.class, () -> {
            bookingService.create(booking, bookerId);
        });

        assertEquals("Владелец не должен  бронировать свою вещь", exception.getMessage());

    }


    @Test
    void findById_BookingNotFoundThrowException() {
        Long bookingId = 999999999L;

        Mockito.when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            bookingService.findById(bookingId);
        });

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
        List<Booking> expectedBookings = List.of(new Booking(), new Booking());

        Mockito.when(userRepository.findById(ownerId)).thenReturn(Optional.of(new User()));
        Mockito.when(bookingRepository.findBookingsByItemOwnerAndEndBeforeOrderByStartDesc(any(), any()))
                .thenReturn(expectedBookings);

        List<Booking> result = bookingService.getByStateAndOwner(state, ownerId);

        assertEquals(expectedBookings, result);
    }

    @Test
    void getBookingByBooker_BookingsFoundSuccess() {
        Long bookerId = 1L;
        State state = State.PAST;
        List<Booking> expectedBookings = List.of(new Booking(), new Booking());

        Mockito.when(userRepository.findById(bookerId)).thenReturn(Optional.of(new User()));
        Mockito.when(bookingRepository.findByBookerAndEndBeforeOrderByStartDesc(any(), any()))
                .thenReturn(expectedBookings);

        List<Booking> result = bookingService.getBookingByBooker(state, bookerId, 0, 10);

        assertEquals(expectedBookings, result);
    }

    @Test
    void getBookingByBooker_FutureBookingsFoundSuccess() {
        Long bookerId = 1L;
        State state = State.FUTURE;
        List<Booking> expectedBookings = List.of(new Booking(), new Booking());

        Mockito.when(userRepository.findById(bookerId)).thenReturn(Optional.of(new User()));
        Mockito.when(bookingRepository.findByBookerAndStartAfterOrderByStartDesc(any(), any()))
                .thenReturn(expectedBookings);

        List<Booking> result = bookingService.getBookingByBooker(state, bookerId, 0, 10);

        assertEquals(expectedBookings, result);
    }

    @Test
    void getBookingByBooker_WaitingBookingsFoundSuccess() {
        Long bookerId = 1L;
        State state = State.WAITING;
        List<Booking> expectedBookings = List.of(new Booking(), new Booking());

        Mockito.when(userRepository.findById(bookerId)).thenReturn(Optional.of(new User()));
        Mockito.when(bookingRepository.findBookingsByItemOwnerAndStatusOrderByStartDesc(any(), (BookingStatus.WAITING)))
                .thenReturn(expectedBookings);

        List<Booking> result = bookingService.getBookingByBooker(state, bookerId, 0, 10);

        assertEquals(expectedBookings, result);
    }

    @Test
    void getBookingByBooker_RejectedBookingsFoundSuccess() {
        Long bookerId = 1L;
        State state = State.REJECTED;
        List<Booking> expectedBookings = List.of(new Booking(), new Booking());

        Mockito.when(userRepository.findById(bookerId)).thenReturn(Optional.of(new User()));
        Mockito.when(bookingRepository.findBookingsByItemOwnerAndStatusOrderByStartDesc(any(), (BookingStatus.REJECTED)))
                .thenReturn(expectedBookings);

        List<Booking> result = bookingService.getBookingByBooker(state, bookerId, 0, 10);

        assertEquals(expectedBookings, result);
    }

    @Test
    void getBookingByBooker_DefaultBookingsFoundSuccess() {
        Long bookerId = 1L;
        State state = State.ALL;
        List<Booking> expectedBookings = List.of(new Booking(), new Booking());

        Mockito.when(userRepository.findById(bookerId)).thenReturn(Optional.of(new User()));
        Mockito.when(bookingRepository.findByBookerOrderByStartDesc(any()))
                .thenReturn(expectedBookings);

        List<Booking> result = bookingService.getBookingByBooker(state, bookerId, 0, 10);

        assertEquals(expectedBookings, result);
    }

}

