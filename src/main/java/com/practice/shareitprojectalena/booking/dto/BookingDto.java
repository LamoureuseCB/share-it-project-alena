package com.practice.shareitprojectalena.booking.dto;

import com.practice.shareitprojectalena.booking.BookingStatus;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingDto {
    private Long id;
    private LocalDateTime start;
    private LocalDateTime end;
    private Long itemId;
    private Long bookerId;
//    @Enumerated
    private BookingStatus bookingStatus;
}