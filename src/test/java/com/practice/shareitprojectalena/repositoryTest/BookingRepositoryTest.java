package com.practice.shareitprojectalena.repositoryTest;

import com.practice.shareitprojectalena.booking.Booking;
import com.practice.shareitprojectalena.booking.BookingRepository;
import com.practice.shareitprojectalena.user.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
public class BookingRepositoryTest {
    @Autowired
    private BookingRepository bookingRepository;

    @Test
    public void findByItemOwnerAndEndBeforeOrderByStartDesc() {
        User owner = new User();
        owner.setId(1L);
        List<Booking> bookings = bookingRepository.findBookingsByItemOwnerAndEndBeforeOrderByStartDesc(owner, LocalDateTime.now());
        assertNotNull(bookings);
        assertTrue(bookings.isEmpty());
    }

    @Test
    public void findByItemOwnerAndStartBeforeAndEndAfterOrderByStartDesc() {
        User owner = new User();
        owner.setId(1L);
        List<Booking> bookings = bookingRepository.findBookingsByItemOwnerAndStartBeforeAndEndAfterOrderByStartDesc(owner, LocalDateTime.now(), LocalDateTime.now());

        assertNotNull(bookings);
        assertTrue(bookings.isEmpty());
    }
}

