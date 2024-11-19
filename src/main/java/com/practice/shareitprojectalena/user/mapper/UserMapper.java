package com.practice.shareitprojectalena.user.mapper;

import com.practice.shareitprojectalena.user.dto.UserCreateDto;
import com.practice.shareitprojectalena.user.dto.UserResponseDto;
import com.practice.shareitprojectalena.user.dto.UserUpdateDto;
import com.practice.shareitprojectalena.user.entity.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

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
        return UserResponseDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }
    public List<UserResponseDto> toResponse(List<User> users){
        return users.stream()
                .map(this::toResponse)
                .toList();
    }

    public void merge(User existing, User updated) {
        if(updated.getName()!= null){
            existing.setName(updated.getName());
        }
        if(updated.getEmail()!= null){
            existing.setEmail(updated.getEmail());
        }

    }
}