package com.practice.shareitserver.user.dto;

import lombok.*;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserCreateDto {
    private String name;
    private String email;
}
