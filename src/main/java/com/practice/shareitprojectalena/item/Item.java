package com.practice.shareitprojectalena.item;


import com.practice.shareitprojectalena.booking.Booking;
import com.practice.shareitprojectalena.item.comment.Comment;
import com.practice.shareitprojectalena.request.entity.ItemRequest;
import com.practice.shareitprojectalena.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.JoinFormula;

import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "items")
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull
    private String name;
    private String description;
    @NotNull
    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable;
    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;
    @OneToMany(mappedBy = "item")
    private List<Booking> bookings;
    @OneToMany(mappedBy = "item")
    private List<Comment> comments = new ArrayList<>();
    @ManyToOne
    @JoinFormula(
            "(select b.id from bookings b " +
                    "where b.item_id = id " +
                    "and b.start_date < localtimestamp(6) " +
                    "and b.status = 'APPROVED' " +
                    "order by b.start_date desc limit 1)")
    private Booking lastBooking;
    @ManyToOne
    @JoinFormula(
            "(select b.id from bookings b " +
                    "where b.item_id = id " +
                    "and b.start_date > localtimestamp(6) " +
                    "and b.status = 'APPROVED' " +
                    "order by b.start_date limit 1)")
    private Booking nextBooking;

    @ManyToOne
    @JoinColumn(name = "request_id")
    private ItemRequest request;
}
