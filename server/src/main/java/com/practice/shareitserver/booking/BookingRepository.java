package com.practice.shareitserver.booking;


import com.practice.shareitserver.user.entity.User;
import com.practice.shareitserver.utils.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {


   Page<Booking> findBookingsByItemOwnerAndEndBeforeOrderByStartDesc(User owner, LocalDateTime end, Pageable pageable);

    Page<Booking> findBookingsByItemOwnerAndStartBeforeAndEndAfterOrderByStartDesc(User owner, LocalDateTime start, LocalDateTime end, Pageable pageable);

    Page<Booking> findBookingsByItemOwnerAndStartAfterOrderByStartDesc(User owner, LocalDateTime start, Pageable pageable);

    Page<Booking> findBookingsByItemOwnerOrderByStartDesc(User owner, Pageable pageable);


    Page<Booking> findBookingsByItemOwnerAndStatusOrderByStartDesc(User owner, BookingStatus status, Pageable pageable);


    Page<Booking> findByBookerOrderByStartDesc(User booker,Pageable pageable);


    Page<Booking> findByBookerAndEndBeforeOrderByStartDesc(User booker, LocalDateTime end,Pageable pageable);


    Page<Booking> findByBookerAndStartBeforeAndEndAfterOrderByStartDesc(User booker, LocalDateTime start, LocalDateTime end,Pageable pageable);


    Page<Booking> findByBookerAndStartAfterOrderByStartDesc(User booker, LocalDateTime start,Pageable pageable);

    boolean existsByItem_IdAndBooker_IdAndStatusAndEndBefore(Long itemId, Long authorId, BookingStatus status, LocalDateTime dateOfBooking);

    Page<Booking> findByItem_IdAndStatusIsAndStartIsBeforeOrderByStartDesc(Long itemId, BookingStatus bookingStatus, LocalDateTime now,Pageable pageable);
}


