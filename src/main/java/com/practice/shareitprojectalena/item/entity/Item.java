package com.practice.shareitprojectalena.item.entity;


import com.practice.shareitprojectalena.user.entity.User;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;




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
    private String name;
    private String description;
    @Column(name = "is_available")
    private Boolean isAvailable;
    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;

//    private List<Booking> bookings;
//    private ItemRequest request;


}
