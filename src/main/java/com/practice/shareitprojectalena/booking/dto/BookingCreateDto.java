package com.practice.shareitprojectalena.booking.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingCreateDto {
    @NotNull
    private Long itemId;
    @FutureOrPresent(message = "Дата начала бронирования не может быть в прошлом.")
    private LocalDateTime start;
    @Future(message = "Дата окончания бронирования должна быть в будущем.")
    private LocalDateTime end;
}



