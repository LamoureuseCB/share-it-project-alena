package com.practice.shareitprojectalena.booking;

import com.practice.shareitprojectalena.utils.BookingStatus;
import com.practice.shareitprojectalena.item.Item;
import com.practice.shareitprojectalena.user.entity.User;

import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "bookings")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "booker_id", nullable = false)
    private User booker;

    @NotNull
    @FutureOrPresent(message = "Дата начала бронирования не может быть в прошлом")
    @Column(name = "start_date")
    private LocalDateTime start;

    @NotNull
    @Future(message = "Дата окончания бронирования должна быть в будущем")
    @Column(name = "end_date")
    private LocalDateTime end;

    @NotNull
    @Enumerated(EnumType.STRING)
    private BookingStatus status;
}
