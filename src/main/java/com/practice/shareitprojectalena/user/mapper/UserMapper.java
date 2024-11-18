package com.practice.shareitprojectalena.user.mapper;

import com.practice.shareitprojectalena.item.dto.ItemCreateDto;
import com.practice.shareitprojectalena.user.dto.UserCreateDto;
import com.practice.shareitprojectalena.user.dto.UserDto;
import com.practice.shareitprojectalena.user.dto.UserResponseDto;
import com.practice.shareitprojectalena.user.dto.UserUpdateDto;
import com.practice.shareitprojectalena.user.entity.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserMapper {

    public static UserDto toDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    public List<UserDto> toDto(List<User> users) {
        return users.stream()
                .map(UserMapper::toDto)
                .collect(Collectors.toList());
    }


    public static User toUser(UserDto userDto) {
        return User.builder()
                .id(userDto.getId())
                .name(userDto.getName())
                .email(userDto.getEmail())
                .build();
    }

    public List<User> toUser(List<UserDto> userDtos) {
        return userDtos.stream()
                .map(UserMapper::toUser)
                .collect(Collectors.toList());
    }


    public static User fromCreate(UserCreateDto userCreateDto) {
        return User.builder()
                .name(userCreateDto.getName())
                .email(userCreateDto.getEmail())
                .build();
    }

    public List<User> fromCreate(List<UserCreateDto> userCreateDtos) {
        return userCreateDtos.stream()
                .map(UserMapper::fromCreate)
                .collect(Collectors.toList());
    }


    public static User fromUpdate(UserUpdateDto userUpdateDto) {
        return User.builder()
                .name(userUpdateDto.getName())
                .email(userUpdateDto.getEmail())
                .build();
    }

    public List<User> fromUpdate(List<UserUpdateDto> userUpdateDtos) {
        return userUpdateDtos.stream()
                .map(UserMapper::fromUpdate)
                .collect(Collectors.toList());
    }

    public static User fromResponse(UserResponseDto userResponseDto) {
        return User.builder()
                .id(userResponseDto.getId())
                .name(userResponseDto.getName())
                .email(userResponseDto.getEmail())
                .build();
    }

    public List<User> fromResponse(List<UserResponseDto> userResponseDtos) {
        return userResponseDtos.stream()
                .map(UserMapper::fromResponse)
                .collect(Collectors.toList());
    }

    public UserResponseDto toResponse(User user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }
}