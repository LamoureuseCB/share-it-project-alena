package com.practice.shareitprojectalena.user.dto;

import lombok.*;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
public class UserUpdateDto {
    private String name;
    private String email;
}
