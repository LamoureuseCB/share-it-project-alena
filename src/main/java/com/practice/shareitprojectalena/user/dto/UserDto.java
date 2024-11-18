package com.practice.shareitprojectalena.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
public class UserDto {
    private Long id;
    private String name;
    private String email;

}
