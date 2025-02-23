package com.practice.shareitgateway.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdateDto {
    @NotBlank(message = "Поле с именем должно быть заполнено")
    private String name;
    @Email(message = "Электронная почта должна содержать символ '@'")
    private String email;
}
