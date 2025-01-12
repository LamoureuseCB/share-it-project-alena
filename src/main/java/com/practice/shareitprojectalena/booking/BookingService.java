package com.practice.shareitprojectalena.booking;

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
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Service
public class BookingService {
    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    public Booking create(Booking booking, Long bookerId) {
        Item item = itemRepository.findById(booking.getItem().getId()).orElseThrow(() -> new NotFoundException("Предмет не найден"));
        if (item.getOwner().getId().equals(bookerId)) {
            throw new ForbiddenException("Владелец не должен  бронировать свою вещь");
        }
        User booker = userRepository.findById(bookerId)
                .orElseThrow(() -> new NotFoundException("Пользователь по данному ID не найден"));

        if (!item.getIsAvailable()) {
            throw new ValidationException("Предмет недоступен для бронирования");
        }
        booking.setBooker(booker);
        booking.setItem(item);
        booking.setStatus(BookingStatus.WAITING);
        System.out.println("Создаю бронирование: " + booking);
        return bookingRepository.save(booking);
    }

    public Booking update(Long bookingId, Long userId,boolean approved) {
        Booking existingBooking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование по данному ID не найдено"));

        Long ownerId = existingBooking.getItem().getOwner().getId();
        if (!ownerId.equals(userId)) {
            throw new ForbiddenException("Обновить бронирование невозможно");
        }
        if (approved) {
            existingBooking.setStatus(BookingStatus.APPROVED);
        } else {
            existingBooking.setStatus(BookingStatus.REJECTED);
        }

        return bookingRepository.save(existingBooking);
    }


    public List<Booking> findAll() {
        return bookingRepository.findAll();
    }


    public void  deleteById(Long bookingId) {
        bookingRepository.deleteById(bookingId);
    }


    public Booking findById(Long bookingId) {
        return bookingRepository.findById(bookingId).orElseThrow(() -> new NotFoundException("Бронирование по данному ID не найдено"));
    }


    public Page<Booking> getByStateAndOwner(State state, Long ownerId, Pageable pageable) {
        Optional<User> owner = userRepository.findById(ownerId);
        if (owner.isEmpty()) {
            throw new ConflictException("Владелец с данным id не найден " + ownerId);
        }

        switch (state) {
            case PAST:
                return bookingRepository.findBookingsByItemOwnerAndEndBeforeOrderByStartDesc(owner.get(), LocalDateTime.now(), pageable);
            case CURRENT:
                return bookingRepository.findBookingsByItemOwnerAndStartBeforeAndEndAfterOrderByStartDesc(owner.get(), LocalDateTime.now(), LocalDateTime.now(), pageable);
            case FUTURE:
                return bookingRepository.findBookingsByItemOwnerAndStartAfterOrderByStartDesc(owner.get(), LocalDateTime.now(), pageable);
            case WAITING:
                return bookingRepository.findBookingsByItemOwnerAndStatusOrderByStartDesc(owner.get(), BookingStatus.WAITING, pageable);
            case REJECTED:
                return bookingRepository.findBookingsByItemOwnerAndStatusOrderByStartDesc(owner.get(), BookingStatus.REJECTED, pageable);
            default:
                return bookingRepository.findBookingsByItemOwnerOrderByStartDesc(owner.get(), pageable);
        }
    }

    public Page<Booking> getBookingByBooker(State state, Long bookerId, int from, int size) {
        User booker = userRepository.findById(bookerId).orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        Pageable pageable = PageRequest.of(from / size, size);
        switch (state) {
            case PAST:
                return bookingRepository.findByBookerAndEndBeforeOrderByStartDesc(booker, LocalDateTime.now(), pageable);
            case CURRENT:
                return bookingRepository.findByBookerAndStartBeforeAndEndAfterOrderByStartDesc(booker, LocalDateTime.now(), LocalDateTime.now(), pageable);
            case FUTURE:
                return bookingRepository.findByBookerAndStartAfterOrderByStartDesc(booker, LocalDateTime.now(), pageable);
            case WAITING:
                return bookingRepository.findBookingsByItemOwnerAndStatusOrderByStartDesc(booker, BookingStatus.WAITING, pageable);
            case REJECTED:
                return bookingRepository.findBookingsByItemOwnerAndStatusOrderByStartDesc(booker, BookingStatus.REJECTED, pageable);
            default:
                return bookingRepository.findByBookerOrderByStartDesc(booker, pageable);
        }
    }




}