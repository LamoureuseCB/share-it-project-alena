package com.practice.shareitprojectalena.user.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
public class UserResponseDto {
    private Long id;
    private String name;
    private String email;
}
