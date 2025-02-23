package com.practice.shareitgateway.booking.dto;

import com.practice.shareitgateway.item.itemDto.ItemResponseDto;
import com.practice.shareitgateway.user.dto.UserResponseDto;
import com.practice.shareitgateway.utils.BookingStatus;
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
