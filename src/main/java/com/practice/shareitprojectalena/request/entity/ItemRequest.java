package com.practice.shareitprojectalena.request.entity;

import com.practice.shareitprojectalena.user.entity.User;

import lombok.*;

import java.time.LocalDateTime;




@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ItemRequest {
    private Long id;
    private String description;
    private User requester;
    private LocalDateTime created;
}