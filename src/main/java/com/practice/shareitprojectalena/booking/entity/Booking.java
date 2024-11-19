package com.practice.shareitprojectalena.booking.entity;

import com.practice.shareitprojectalena.booking.BookingStatus;
import com.practice.shareitprojectalena.item.entity.Item;
import com.practice.shareitprojectalena.user.entity.User;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter

@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Booking {

    private Long id;
    private LocalDateTime start;
    private LocalDateTime end;
    private Item item;
    private User booker;
    private BookingStatus bookingStatus;


}
