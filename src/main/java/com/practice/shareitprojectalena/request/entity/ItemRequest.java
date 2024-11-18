package com.practice.shareitprojectalena.request.entity;

import com.practice.shareitprojectalena.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ItemRequest {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;
    private String description;
    private User requester;
    private LocalDateTime created;
}