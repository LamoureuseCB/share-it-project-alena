package com.practice.shareitprojectalena.user.dto;

import jakarta.validation.constraints.Email;
import lombok.*;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
public class UserUpdateDto {
    private String name;
    @Email
    private String email;
}
