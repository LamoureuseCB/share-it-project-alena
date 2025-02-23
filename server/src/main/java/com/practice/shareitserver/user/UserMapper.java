package com.practice.shareitserver.user;

import com.practice.shareitserver.user.dto.UserCreateDto;
import com.practice.shareitserver.user.dto.UserResponseDto;
import com.practice.shareitserver.user.dto.UserUpdateDto;
import com.practice.shareitserver.user.entity.User;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserMapper {


    public User fromCreate(UserCreateDto userCreateDto) {
        return User.builder()
                .name(userCreateDto.getName())
                .email(userCreateDto.getEmail())
                .build();
    }


    public User fromUpdate(UserUpdateDto userUpdateDto) {
        return User.builder()
                .name(userUpdateDto.getName())
                .email(userUpdateDto.getEmail())
                .build();
    }

    public UserResponseDto toResponse(User user) {
            if (user == null) {
                System.out.println("Пользователь не указан!");
                return null;
            }

            return new UserResponseDto(user.getId(), user.getName(), user.getEmail());
        }


    public List<UserResponseDto> toResponse(List<User> users) {
        return users.stream()
                .map(this::toResponse)
                .toList();
    }

    public void merge(User existing, User updated) {
        if (updated.getName() != null) {
            existing.setName(updated.getName());
        }

        if (updated.getEmail() != null) {
            existing.setEmail(updated.getEmail());
        }

    }
}