package com.practice.shareitprojectalena.booking.entity;

import com.practice.shareitprojectalena.booking.BookingStatus;
import com.practice.shareitprojectalena.item.entity.Item;
import com.practice.shareitprojectalena.user.entity.User;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Booking {
    @Id
    @GeneratedValue
    private Long id;
    private LocalDateTime start;
    private LocalDateTime end;
    private Item item;
    private User booker;
    @Enumerated
    private BookingStatus bookingStatus;


}
