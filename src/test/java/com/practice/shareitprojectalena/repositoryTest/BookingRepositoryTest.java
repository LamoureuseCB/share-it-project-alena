package com.practice.shareitprojectalena.repositoryTest;

import com.practice.shareitprojectalena.booking.Booking;
import com.practice.shareitprojectalena.booking.BookingRepository;
import com.practice.shareitprojectalena.user.UserRepository;
import com.practice.shareitprojectalena.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class BookingRepositoryTest {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    private User owner;

    @BeforeEach
    public void setUp() {
        owner = new User();
        owner.setId(1L);
        owner.setName("Тестовый владелец");
        userRepository.save(owner);

        Booking booking1 = new Booking();
        booking1.setId(1L);
        booking1.setStart(LocalDateTime.now().minusDays(2));
        booking1.setEnd(LocalDateTime.now().minusDays(1));

        Booking booking2 = new Booking();
        booking2.setId(2L);
        booking2.setStart(LocalDateTime.now().minusDays(5));
        booking2.setEnd(LocalDateTime.now().minusDays(3));

        bookingRepository.save(booking1);
        bookingRepository.save(booking2);
    }

    @Test
    public void findByItemOwnerAndEndBeforeOrderByStartDesc() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Booking> bookings = bookingRepository.findBookingsByItemOwnerAndEndBeforeOrderByStartDesc(owner, LocalDateTime.now(), pageable);

        assertNotNull(bookings);
        assertTrue(bookings.hasContent());
        assertEquals(2, bookings.getTotalElements());
    }

    @Test
    public void findByItemOwnerAndStartBeforeAndEndAfterOrderByStartDesc() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Booking> bookings = bookingRepository.findBookingsByItemOwnerAndStartBeforeAndEndAfterOrderByStartDesc(owner, LocalDateTime.now(), LocalDateTime.now(), pageable);

        assertNotNull(bookings);
        assertTrue(bookings.isEmpty()); }
}
