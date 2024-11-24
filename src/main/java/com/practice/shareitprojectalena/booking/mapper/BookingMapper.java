package com.practice.shareitprojectalena.booking.mapper;

import com.practice.shareitprojectalena.utils.BookingStatus;
import com.practice.shareitprojectalena.booking.dto.BookingCreateDto;
import com.practice.shareitprojectalena.booking.dto.BookingResponseDto;
import com.practice.shareitprojectalena.booking.entity.Booking;
import com.practice.shareitprojectalena.item.entity.Item;
import com.practice.shareitprojectalena.item.mapper.ItemMapper;
import com.practice.shareitprojectalena.user.entity.User;
import com.practice.shareitprojectalena.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BookingMapper {

    private final ItemMapper itemMapper;
    private final UserMapper userMapper;


    public Booking fromCreate(BookingCreateDto bookingCreateDto, Item item) {
        return Booking.builder()
                .item(item)
                .start(bookingCreateDto.getStart())
                .end(bookingCreateDto.getEnd())
                .status(BookingStatus.WAITING)
                .build();
    }


    public Booking fromUpdate(BookingCreateDto bookingCreateDto, Item item, User booker) {
        return Booking.builder()
                .item(item)
                .start(bookingCreateDto.getStart())
                .end(bookingCreateDto.getEnd())
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();
    }


    public BookingResponseDto toResponse(Booking booking) {
        return BookingResponseDto.builder()
                .itemId(booking.getItem().getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .status(booking.getStatus())
                .booker(userMapper.toResponse(booking.getBooker()))
                .item(itemMapper.toResponse(booking.getItem()))
                .build();
    }

    public void merge(Booking existingBooking, Booking updatedBooking) {
        if (updatedBooking.getStart() != null) {
            existingBooking.setStart(updatedBooking.getStart());
        }
        if (updatedBooking.getEnd() != null) {
            existingBooking.setEnd(updatedBooking.getEnd());
        }

        if (updatedBooking.getStatus() != null) {
            existingBooking.setStatus(updatedBooking.getStatus());
        }

        if (updatedBooking.getItem() != null && !existingBooking.getItem().equals(updatedBooking.getItem())) {
            existingBooking.setItem(updatedBooking.getItem());
        }
        if (updatedBooking.getBooker() != null && !existingBooking.getBooker().equals(updatedBooking.getBooker())) {
            existingBooking.setBooker(updatedBooking.getBooker());
        }
    }
}
