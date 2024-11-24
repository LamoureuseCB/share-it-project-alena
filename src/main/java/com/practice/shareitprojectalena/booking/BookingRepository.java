package com.practice.shareitprojectalena.booking;

import com.practice.shareitprojectalena.utils.BookingStatus;
import com.practice.shareitprojectalena.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {


    List<Booking> findBookingsByItemOwnerAndEndBeforeOrderByStartDesc(User owner, LocalDateTime end);

    List<Booking> findBookingsByItemOwnerAndStartBeforeAndEndAfterOrderByStartDesc(User owner, LocalDateTime start, LocalDateTime end);

    List<Booking> findBookingsByItemOwnerAndStartAfterOrderByStartDesc(User owner, LocalDateTime start);

    List<Booking> findBookingsByItemOwnerOrderByStartDesc(User owner);


    List<Booking> findBookingsByItemOwnerAndStatusOrderByStartDesc(User owner, BookingStatus status);


    List<Booking> findByBookerOrderByStartDesc(User booker);


    List<Booking> findByBookerAndEndBeforeOrderByStartDesc(User booker, LocalDateTime end);


    List<Booking> findByBookerAndStartBeforeAndEndAfterOrderByStartDesc(User booker, LocalDateTime start, LocalDateTime end);


    List<Booking> findByBookerAndStartAfterOrderByStartDesc(User booker, LocalDateTime start);

    boolean findByItem_IdAndBooker_IdAndStatusAndEndBefore(Long itemId, Long authorId, BookingStatus status, LocalDateTime dateOfBooking);

}


