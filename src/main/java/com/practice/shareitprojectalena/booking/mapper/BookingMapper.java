package com.practice.shareitprojectalena.booking.mapper;

import com.practice.shareitprojectalena.booking.dto.BookingDto;
import com.practice.shareitprojectalena.booking.entity.Booking;
import org.springframework.stereotype.Component;

@Component
public class BookingMapper {
    public static BookingDto toBookingDto(Booking booking){
        return BookingDto.builder()
                .id(booking.getId())
                .bookerId(booking.getBooker().getId())
                .build();
    }
    public static Booking toBooking(BookingDto bookingDto){
        return Booking.builder()
                .id(bookingDto.getId())
                .start(bookingDto.getStart())
                .end(bookingDto.getEnd())
                .bookingStatus(bookingDto.getBookingStatus())
               .build();
    }
}
