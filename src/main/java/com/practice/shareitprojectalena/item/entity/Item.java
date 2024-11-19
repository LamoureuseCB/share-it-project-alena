package com.practice.shareitprojectalena.item.entity;


import com.practice.shareitprojectalena.user.entity.User;

import lombok.*;




@Getter
@Setter

@Builder
@NoArgsConstructor
@AllArgsConstructor

public class Item {

    private Long id;
    private String name;
    private String description;
    private Boolean isAvailable;
    private User owner;
//    private List<Booking> bookings;
//    private ItemRequest request;


}
