package com.practice.shareitgateway.user;

import com.practice.shareitgateway.user.dto.UserCreateDto;
import com.practice.shareitgateway.user.dto.UserResponseDto;
import com.practice.shareitgateway.user.dto.UserUpdateDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import static com.practice.shareitgateway.utils.RequestConstants.SERVER_URL;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UserGatewayController {
    private final RestTemplate restTemplate;

    @PostMapping
    public UserResponseDto create(@Valid @RequestBody UserCreateDto userCreateDto) {
        log.info("Получен запрос на создание пользователя в Gateway: {}", userCreateDto);
        UserResponseDto response = restTemplate.postForObject(SERVER_URL + "/users", userCreateDto, UserResponseDto.class);
        log.info("Ответ от сервера: {}", response);
        return response;
    }

    @PatchMapping("/{id}")
    public UserResponseDto update(@PathVariable Long id, @Valid @RequestBody UserUpdateDto userUpdateDto) {
        log.info("Получен запрос на обновление информации пользователя в Gateway: {}", userUpdateDto);
        return restTemplate.postForObject(SERVER_URL + id, userUpdateDto, UserResponseDto.class);
    }

    @GetMapping("/{id}")
    public UserResponseDto getUserById(@PathVariable Long id) {
        if (id <= 0) {
            throw new IllegalArgumentException("ID пользователя должен быть больше нуля");
        }
        return restTemplate.getForObject(SERVER_URL + id, UserResponseDto.class);
    }

}
