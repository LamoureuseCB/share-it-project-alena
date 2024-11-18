package com.practice.shareitprojectalena.user.dto;

import lombok.*;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
public class UserCreateDto {
    private Long id;
    private String name;
    private String email;
}
