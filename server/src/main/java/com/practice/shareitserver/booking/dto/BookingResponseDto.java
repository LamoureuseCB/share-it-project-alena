package com.practice.shareitserver.booking.dto;

import com.practice.shareitserver.item.itemDto.ItemResponseDto;
import com.practice.shareitserver.user.dto.UserResponseDto;
import com.practice.shareitserver.utils.BookingStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BookingResponseDto {
    private Long id;
    private Long itemId;
    private LocalDateTime start;
    private LocalDateTime end;
    private BookingStatus status;
    private UserResponseDto booker;
    private ItemResponseDto item;


}
