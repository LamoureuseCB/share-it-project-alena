package com.practice.shareitprojectalena.user;

import com.practice.shareitprojectalena.user.dto.UserCreateDto;
import com.practice.shareitprojectalena.user.dto.UserResponseDto;
import com.practice.shareitprojectalena.user.dto.UserUpdateDto;
import com.practice.shareitprojectalena.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserMapper userMapper;
    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponseDto create(@Valid @RequestBody UserCreateDto userCreateDto) {
        User user = userMapper.fromCreate(userCreateDto);
        User createdUser = userService.create(user);
        return userMapper.toResponse(createdUser);
    }

    @GetMapping("/{id}")
    public UserResponseDto findById(@PathVariable Long id) {
        User user = userService.findById(id);
        return userMapper.toResponse(user);
    }

    @GetMapping
    public List<UserResponseDto> findAll() {
        List<User> users = userService.findAll();
        return users.stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @PatchMapping("/{id}")
    public UserResponseDto update(@PathVariable Long id, @Valid @RequestBody UserUpdateDto userUpdateDto) {
        User user = userMapper.fromUpdate(userUpdateDto);
        User updatedUser = userService.update(user, id);
        return userMapper.toResponse(updatedUser);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        userService.delete(id);
    }
}

