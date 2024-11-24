package com.practice.shareitprojectalena.booking.service;

import com.practice.shareitprojectalena.utils.BookingStatus;
import com.practice.shareitprojectalena.booking.entity.Booking;
import com.practice.shareitprojectalena.booking.mapper.BookingMapper;
import com.practice.shareitprojectalena.booking.repository.BookingRepository;
import com.practice.shareitprojectalena.error.exceptions.ConflictException;
import com.practice.shareitprojectalena.error.exceptions.NotFoundException;
import com.practice.shareitprojectalena.item.entity.Item;
import com.practice.shareitprojectalena.item.service.ItemService;
import com.practice.shareitprojectalena.user.entity.User;
import com.practice.shareitprojectalena.user.repository.UserRepository;
import com.practice.shareitprojectalena.utils.State;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Service
public class BookingService {
    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;
    private final UserRepository userRepository;
    private final ItemService itemService;

    public Booking create(Booking booking, Long bookerId, Item item) {
        if (!item.getIsAvailable()) {
            throw new ConflictException(item.getName() + " недоступен для бронирования");
        }
        User booker = userRepository.findById(bookerId)
                .orElseThrow(() -> new NotFoundException("Пользователь по данному ID не найден"));
        booking.setBooker(booker);
        booking.setItem(item);
        return bookingRepository.save(booking);
    }

    public Booking update(Long bookingId, Long bookerId,boolean approved ) {
        Booking existingBooking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование по данному ID не найдено"));

        if (!existingBooking.getBooker().getId().equals(bookerId)) {
            throw new NotFoundException("Обновить бронирование невозможно");
        }
        if(approved){
            existingBooking.setStatus(BookingStatus.APPROVED);
        }
        else{
            existingBooking.setStatus(BookingStatus.REJECTED);
        }

        return bookingRepository.save(existingBooking);
    }


    public List<Booking> findAll() {
        return bookingRepository.findAll();
    }


    void deleteById(Long bookingId) {
        bookingRepository.deleteById(bookingId);
    }


    public Booking findById(Long bookingId) {
        return bookingRepository.findById(bookingId).orElseThrow(() -> new NotFoundException("Бронирование по данному ID не найдено"));
    }

    public List<Booking> getByStateAndOwner(State state, Long ownerId) {
        List<Booking> bookings = new ArrayList<>();
        Optional<User> owner = userRepository.findById(ownerId);
        if (owner.isEmpty()) {
            throw new ConflictException("Владелец с данным id не найден" + ownerId);
        }
        switch (state) {
            case PAST ->
                    bookings = bookingRepository.findBookingsByItemOwnerAndEndBeforeOrderByStartDesc(owner.get(), LocalDateTime.now());
            case CURRENT ->
                    bookings = bookingRepository.findBookingsByItemOwnerAndStartBeforeAndEndAfterOrderByStartDesc(owner.get(), LocalDateTime.now(), LocalDateTime.now());
            case FUTURE ->
                    bookings = bookingRepository.findBookingsByItemOwnerAndStartAfterOrderByStartDesc(owner.get(), LocalDateTime.now());
            case WAITING ->
                    bookings = bookingRepository.findBookingsByItemOwnerAndStatusOrderByStartDesc(owner.get(), BookingStatus.WAITING);
            case REJECTED ->
                    bookings = bookingRepository.findBookingsByItemOwnerAndStatusOrderByStartDesc(owner.get(), BookingStatus.REJECTED);
            default -> bookings = bookingRepository.findBookingsByItemOwnerOrderByStartDesc(owner.get());
        }
        return bookings;
    }

    public List<Booking> getBookingByOwner(State state, Long ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        List<Booking> bookingsByOwner = new ArrayList<>();
        switch (state) {
            case PAST -> {
                bookingsByOwner =  bookingRepository.findByBookerAndEndBeforeOrderByStartDesc(owner, LocalDateTime.now());
            }
            case CURRENT ->
                    bookingsByOwner =  bookingRepository.findByBookerAndStartBeforeAndEndAfterOrderByStartDesc(owner, LocalDateTime.now(), LocalDateTime.now());
            case FUTURE ->
            bookingsByOwner =  bookingRepository.findByBookerAndStartAfterOrderByStartDesc(owner, LocalDateTime.now());
            case WAITING ->
                    bookingsByOwner =  bookingRepository.findBookingsByItemOwnerAndStatusOrderByStartDesc(owner, BookingStatus.WAITING);
            case REJECTED ->
                    bookingsByOwner =  bookingRepository.findBookingsByItemOwnerAndStatusOrderByStartDesc(owner, BookingStatus.REJECTED);
            default ->
                    bookingsByOwner =  bookingRepository.findByBookerOrderByStartDesc(owner);
        }
        return bookingsByOwner;
    }






}