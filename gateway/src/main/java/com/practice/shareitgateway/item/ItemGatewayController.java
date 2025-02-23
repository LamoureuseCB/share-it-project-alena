package com.practice.shareitgateway.item;

import com.practice.shareitgateway.error.exceptions.ItemUpdateException;
import com.practice.shareitgateway.error.exceptions.NotFoundException;
import com.practice.shareitgateway.item.itemDto.ItemCreateDto;
import com.practice.shareitgateway.item.itemDto.ItemResponseDto;
import com.practice.shareitgateway.item.itemDto.ItemUpdateDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

import static com.practice.shareitgateway.utils.RequestConstants.SERVER_URL;
import static com.practice.shareitgateway.utils.RequestConstants.USER_HEADER;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Slf4j
public class ItemGatewayController {
    private final RestTemplate restTemplate;

    private HttpHeaders createHeaders(Long userId) {
        HttpHeaders headers = new HttpHeaders();
        if (userId != null) {
            headers.set(USER_HEADER, String.valueOf(userId));
        }
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }


    @PostMapping
    public ItemResponseDto create(@RequestHeader(USER_HEADER) Long userId,
                                  @RequestBody @Valid ItemCreateDto itemCreateDto) {
        if (userId == null) {
            throw new IllegalArgumentException("Поле с ID пользователя должно быть заполнено");
        }
        log.info("Создание вещи пользователем с ID: {}", userId);
        HttpHeaders headers = createHeaders(userId);
        HttpEntity<ItemCreateDto> request = new HttpEntity<>(itemCreateDto, headers);
        return restTemplate.postForObject(SERVER_URL + "/items", request, ItemResponseDto.class);
    }

    @PatchMapping("/{itemId}")
    public ItemResponseDto update(@RequestHeader(USER_HEADER) Long userId,
                                  @PathVariable Long itemId,
                                  @RequestBody @Valid ItemUpdateDto itemUpdateDto) {
        if (userId == null || itemId == null) {
            throw new IllegalArgumentException("ID пользователя или бронируемой вещи не могут быть null");
        }
        HttpHeaders headers = createHeaders(userId);
        HttpEntity<ItemUpdateDto> request = new HttpEntity<>(itemUpdateDto, headers);

        ItemResponseDto responseDto = restTemplate.patchForObject(
                SERVER_URL + "/items/" + itemId, request, ItemResponseDto.class);

        if (responseDto == null) {
            throw new ItemUpdateException("Ошибка обновления вещи: сервер вернул null");
        }
        return responseDto;
    }

    @GetMapping
    public List<ItemResponseDto> findAll(@RequestHeader(USER_HEADER) Long userId,
                                         @RequestParam(defaultValue = "0") int from,
                                         @RequestParam(defaultValue = "10") int size) {
        if(from < 0 || size < 0) {
            throw new IllegalArgumentException("Параметры страниц должны быть указаны больше 0");
        }
        log.info("Получение списка всех вещей пользователя с ID {} (from={}, size={})", userId, from, size);
        HttpHeaders headers = createHeaders(userId);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ItemResponseDto[] response = restTemplate.getForObject(
                SERVER_URL + "/items?from=" + from + "&size=" + size,
                ItemResponseDto[].class,
                request
        );
        if (response != null && response.length > 0) {
            return List.of(response);

        }
        return Collections.emptyList();

    }



    @GetMapping("/search")
    public List<ItemResponseDto> searchItems(@RequestParam String text,
                                             @RequestParam(defaultValue = "0") int from,
                                             @RequestParam(defaultValue = "10") int size) {
        if (from < 0 || size < 0) {
            throw new IllegalArgumentException("Параметры страниц должны быть больше 0");
        }

        log.info("Поиск вещей по тексту '{}', from={}, size={}", text, from, size);

        try {
            ItemResponseDto[] response = restTemplate.getForObject(
                    SERVER_URL + "/items/search?text=" + text + "&from=" + from + "&size=" + size,
                    ItemResponseDto[].class
            );

            if (response != null && response.length > 0) {
                return List.of(response);
            }
        } catch (HttpClientErrorException.NotFound exception) {
            log.warn("Ошибка, сервер не найден при поиске текста  '{}'", text);
            throw new NotFoundException("Ошибка! Сервер не найден");

        } catch (HttpServerErrorException exception) {
            log.error("Внутренняя ошибка сервера");
            throw new HttpServerErrorException(exception.getStatusCode());
        }

        return Collections.emptyList();
    }

}
