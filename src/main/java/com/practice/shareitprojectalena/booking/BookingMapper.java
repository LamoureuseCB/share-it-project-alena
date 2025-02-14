package com.practice.shareitprojectalena.booking;

import com.practice.shareitprojectalena.booking.dto.BookingCreateDto;
import com.practice.shareitprojectalena.booking.dto.BookingResponseDto;
import com.practice.shareitprojectalena.error.exceptions.ValidationException;
import com.practice.shareitprojectalena.item.Item;
import com.practice.shareitprojectalena.item.ItemMapper;
import com.practice.shareitprojectalena.item.ItemService;
import com.practice.shareitprojectalena.user.UserMapper;
import com.practice.shareitprojectalena.user.entity.User;
import com.practice.shareitprojectalena.utils.BookingStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class BookingMapper {

    private final ItemMapper itemMapper;
    private final UserMapper userMapper;
    private final ItemService itemService;

    public Booking fromCreate(BookingCreateDto bookingCreateDto) {
        if (bookingCreateDto == null) {
            throw new IllegalArgumentException("Поля для создания бронирования должны быть заполнены");
        }
        if (bookingCreateDto.getItemId() == null) {
            throw new IllegalArgumentException("Предмет для бронирования не указан");
        }

        Item item = itemService.findById(bookingCreateDto.getItemId());


        if (item == null) {
            throw new IllegalArgumentException("Предмет с таким ID не найден");
        }

        if (item.getOwner() == null) {
            throw new IllegalArgumentException("Владелец предмета не указан");
        }
        if (!item.getIsAvailable()) {
            throw new ValidationException("Предмет недоступен для бронирования");
        }

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
        if (booking == null) {
            throw new IllegalArgumentException("Бронирование отсутствует");
        }

        if (booking.getBooker() == null) {
            throw new IllegalStateException ("Создающий бронирование пользователь отсутствует!");
        }

        if (booking.getItem() == null) {
            throw new IllegalStateException("Предмет для бронирования отсутствует! ");
        }

        if (booking.getItem().getOwner() == null) {
            throw new IllegalStateException("Владелец предмета отсутствует!");
        }

        return BookingResponseDto.builder()
                .id(booking.getId())
                .itemId(booking.getItem().getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .status(booking.getStatus())
                .booker(userMapper.toResponse(booking.getBooker()))
                .item(itemMapper.toResponse(booking.getItem()))
                .build();
    }

}
