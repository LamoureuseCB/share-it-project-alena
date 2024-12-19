package com.practice.shareitprojectalena.serviceTest;

import com.practice.shareitprojectalena.booking.Booking;
import com.practice.shareitprojectalena.booking.BookingRepository;
import com.practice.shareitprojectalena.booking.BookingService;
import com.practice.shareitprojectalena.error.exceptions.ConflictException;
import com.practice.shareitprojectalena.error.exceptions.ForbiddenException;
import com.practice.shareitprojectalena.error.exceptions.NotFoundException;
import com.practice.shareitprojectalena.error.exceptions.ValidationException;
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

        User owner = new User();
        owner.setId(1l);
        User booker = new User();
        booker.setId(2l);
        Item item = new Item();
        booking.setItem(item);
        item.setIsAvailable(true);
        item.setOwner(owner);

        Mockito.when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));
        Mockito.when(bookingRepository.save(booking)).thenReturn(booking);
        Mockito.when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));


        Booking bookingResult = bookingService.create(booking, booker.getId());
        assertEquals(booking, bookingResult);
    }

    @Test
    void create_ItemNotAvailableThrowValidateException() {
        Booking booking = new Booking();
        User booker = new User();
        booker.setId(1L);
        User owner = new User();
        owner.setId(2L);
        Item item = new Item();
        item.setId(1L);
        item.setOwner(owner);

        booking.setItem(item);
        booking.setBooker(booker);
        item.setIsAvailable(false);
        Mockito.when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        Mockito.when((userRepository.findById(booker.getId()))).thenReturn(Optional.of(booker));

       ValidationException exception = assertThrows(ValidationException.class, () -> {
            bookingService.create(booking, booker.getId());
        });

        assertEquals("Предмет недоступен для бронирования", exception.getMessage());
    }

    @Test
    void create_BookingBookerNotFoundThrowNotFoundException() {
        Booking booking = new Booking();
        Long bookerId = 100000L;
        User owner = new User();
        owner.setId(1l);
        Item item = new Item();
        item.setIsAvailable(true);
        booking.setItem(item);
        item.setOwner(owner);

        Mockito.when(userRepository.findById(bookerId)).thenReturn(Optional.empty());
        Mockito.when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

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
        Mockito.when(bookingRepository.save(existingBooking)).thenReturn(existingBooking);

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
    void update_ThrownForbiddenException_IfUserIsNotOwner() {
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
        Long ownerId = 1L;
        Long bookerId = 3L;
        boolean approved = false;

        Booking existingBooking = new Booking();
        existingBooking.setItem(new Item());
        existingBooking.getItem().setOwner(new User());
        existingBooking.getItem().getOwner().setId(ownerId);
        existingBooking.setStatus(BookingStatus.WAITING);

        Mockito.when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(existingBooking));

        ForbiddenException exception = assertThrows(ForbiddenException.class, () -> {
            bookingService.update(bookingId, bookerId, approved);
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
    void create_BookingItemNotFoundThrowNotFoundException() {
        Booking booking = new Booking();
        Long bookerId = 1L;
        Item item = new Item();
        item.setId(1L);
        booking.setItem(item);

        Mockito.when(itemRepository.findById(item.getId())).thenReturn(Optional.empty());


        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            bookingService.create(booking, bookerId);
        });

        assertEquals("Предмет не найден", exception.getMessage());


    }


    @Test
    void create_BookingIfBookerIsItemOwnerThrowForbiddenException() {
        Booking booking = new Booking();
        Long bookerId = 1L;
        Item item = new Item();
        item.setId(1L);
        booking.setItem(item);
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
        User user = new User();
        user.setName("User1");

        Mockito.when(userRepository.findById(bookerId)).thenReturn(Optional.of(user));
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
        User user = new User();
        user.setName("User1");

        Mockito.when(userRepository.findById(bookerId)).thenReturn(Optional.of(user));
        Mockito.when(bookingRepository.findBookingsByItemOwnerAndStatusOrderByStartDesc(user, BookingStatus.WAITING))
                .thenReturn(expectedBookings);

        List<Booking> result = bookingService.getBookingByBooker(state, bookerId, 0, 10);

        assertEquals(expectedBookings, result);
    }

    @Test
    void getBookingByBooker_RejectedBookingsFoundSuccess() {
        Long bookerId = 1L;
        State state = State.REJECTED;
        List<Booking> expectedBookings = List.of(new Booking(), new Booking());
        User user = new User();
        user.setName("User1");

        Mockito.when(userRepository.findById(bookerId)).thenReturn(Optional.of(user));
        Mockito.when(bookingRepository.findBookingsByItemOwnerAndStatusOrderByStartDesc(user, BookingStatus.REJECTED))
                .thenReturn(expectedBookings);

        List<Booking> result = bookingService.getBookingByBooker(state, bookerId, 0, 10);

        assertEquals(expectedBookings, result);
    }

    @Test
    void getBookingByBooker_DefaultBookingsFoundSuccess() {
        Long bookerId = 1L;
        State state = State.ALL;
        List<Booking> expectedBookings = List.of(new Booking(), new Booking());
        User user = new User();
        user.setName("User1");

        Mockito.when(userRepository.findById(bookerId)).thenReturn(Optional.of(user));
        Mockito.when(bookingRepository.findByBookerOrderByStartDesc(user))
                .thenReturn(expectedBookings);

        List<Booking> result = bookingService.getBookingByBooker(state, bookerId, 0, 10);

        assertEquals(expectedBookings, result);
    }

}

