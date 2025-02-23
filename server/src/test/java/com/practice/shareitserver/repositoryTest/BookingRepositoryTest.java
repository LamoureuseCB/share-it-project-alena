package com.practice.shareitserver.repositoryTest;


import com.practice.shareitserver.booking.Booking;
import com.practice.shareitserver.booking.BookingRepository;
import com.practice.shareitserver.item.Item;
import com.practice.shareitserver.item.ItemRepository;
import com.practice.shareitserver.user.UserRepository;
import com.practice.shareitserver.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class BookingRepositoryTest {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRepository itemRepository;

    private User owner;

    @BeforeEach
    public void setUp() {
        owner = new User();
        owner.setName("Тестовый владелец");

        Item item = new Item();
        item.setName("Тестовая вещь");
        item.setOwner(owner);
        item.setIsAvailable(true);
        owner.setItems(List.of(item));
        owner = userRepository.save(owner);
        itemRepository.save(item);


        Booking booking1 = new Booking();
        booking1.setBooker(owner);
        booking1.setItem(item);
        booking1.setStart(LocalDateTime.now().minusDays(2));
        booking1.setEnd(LocalDateTime.now().minusDays(1));

        Booking booking2 = new Booking();
        booking2.setBooker(owner);
        booking2.setItem(item);
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
        assertTrue(bookings.isEmpty());
    }
}
