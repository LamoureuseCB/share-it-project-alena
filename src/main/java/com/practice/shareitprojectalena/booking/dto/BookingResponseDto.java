package com.practice.shareitprojectalena.booking.dto;

import com.practice.shareitprojectalena.utils.BookingStatus;
import com.practice.shareitprojectalena.item.dto.ItemResponseDto;
import com.practice.shareitprojectalena.user.dto.UserResponseDto;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
@Getter
@Builder
public class BookingResponseDto {
    private Long itemId;
    private LocalDateTime start;
    private LocalDateTime end;
    private BookingStatus status;
    private UserResponseDto booker;
    private ItemResponseDto item;
}