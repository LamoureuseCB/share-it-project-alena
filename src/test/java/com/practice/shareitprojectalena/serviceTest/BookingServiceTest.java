package com.practice.shareitprojectalena.serviceTest;

import com.practice.shareitprojectalena.booking.Booking;
import com.practice.shareitprojectalena.booking.BookingRepository;
import com.practice.shareitprojectalena.booking.BookingService;
import com.practice.shareitprojectalena.error.exceptions.ConflictException;
import com.practice.shareitprojectalena.error.exceptions.NotFoundException;
import com.practice.shareitprojectalena.item.Item;
import com.practice.shareitprojectalena.user.UserRepository;
import com.practice.shareitprojectalena.user.entity.User;
import com.practice.shareitprojectalena.utils.BookingStatus;
import com.practice.shareitprojectalena.utils.State;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class BookingServiceTest {
    @InjectMocks
    private BookingService bookingService;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private UserRepository userRepository;

    @Test
    void create_BookingSuccess() {
        Booking booking = new Booking();
        Long bookerId = 1L;
        Item item = new Item();
        item.setIsAvailable(true);

        when(userRepository.findById(bookerId)).thenReturn(Optional.of(new User()));
        when(bookingRepository.save(booking)).thenReturn(booking);

        Booking bookingResult = bookingService.create(booking, bookerId, item);
        assertEquals(booking, bookingResult);
    }

    @Test
    void create_ItemNotAvailableThrowConflictException() {
        Booking booking = new Booking();
        Long bookerId = 1L;
        Item item = new Item();
        item.setIsAvailable(false);

        ConflictException exception = assertThrows(ConflictException.class, () -> {
            bookingService.create(booking, bookerId, item);
        });

        assertEquals(item.getName() + " недоступен для бронирования", exception.getMessage());
    }

    @Test
    void create_BookingBookerNotFoundThrowNotFoundException() {
        Booking booking = new Booking();
        Long bookerId = 100000L;
        Item item = new Item();
        item.setIsAvailable(true);

        when(userRepository.findById(bookerId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            bookingService.create(booking, bookerId, item);
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

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(existingBooking));

        Booking bookingResult = bookingService.update(bookingId, userId, approved);

        assertEquals(BookingStatus.APPROVED, bookingResult.getStatus());
    }

    @Test
    void update_BookingNotFoundThrowNotFoundException() {
        Long bookingId = 100000L;
        Long userId = 1L;
        boolean approved = true;

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

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

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(existingBooking));

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            bookingService.update(bookingId, userId, approved);
        });

        assertEquals("Обновить бронирование невозможно", exception.getMessage());
    }

    @Test
    void findAll_ReturnsAllBookings() {
        List<Booking> expected = List.of(new Booking(), new Booking());
        when(bookingRepository.findAll()).thenReturn(expected);
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
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));

        List<Booking> expected = List.of(new Booking(), new Booking());
        when(bookingRepository.findBookingsByItemOwnerAndStartAfterOrderByStartDesc(any(), any())).thenReturn(expected);

        List<Booking> result = bookingService.getByStateAndOwner(State.FUTURE, 1L);
        assertEquals(expected, result);
    }


}

