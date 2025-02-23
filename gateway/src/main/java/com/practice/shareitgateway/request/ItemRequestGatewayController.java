package com.practice.shareitgateway.request;


import com.practice.common.mapper.RequestFullDtoMapper;
import com.practice.common.dto.ItemRequestFullResponseDto;
import com.practice.shareitgateway.request.dto.ItemRequestCreateDto;
import com.practice.common.dto.ItemRequestFullDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.practice.shareitgateway.utils.RequestConstants.SERVER_URL;
import static com.practice.shareitgateway.utils.RequestConstants.USER_HEADER;


@RestController
@RequiredArgsConstructor
@RequestMapping("/requests")
@Slf4j
public class ItemRequestGatewayController {
    private final RestTemplate restTemplate;
    private final RequestFullDtoMapper requestFullDtoMapper;

    private HttpHeaders createHeaders(Long userId) {
        HttpHeaders headers = new HttpHeaders();
        if (userId != null) {
            headers.set(USER_HEADER, String.valueOf(userId));
        }
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemRequestFullResponseDto create(@RequestHeader(USER_HEADER) Long userId,
                                             @Valid @RequestBody ItemRequestCreateDto itemRequestCreateDto) {
        HttpHeaders headers = createHeaders(userId);
        HttpEntity<ItemRequestCreateDto> request = new HttpEntity<>(itemRequestCreateDto, headers);
        ItemRequestFullDto response = restTemplate.postForObject(SERVER_URL + "/requests", request, ItemRequestFullDto.class);
        if(response!= null){
            return requestFullDtoMapper.mapToResponseDto(response);
        }
        log.warn("Сервер вернул пустой ответ");
        return null;
    }


    @GetMapping
    public List<ItemRequestFullResponseDto> getAllRequests(@RequestHeader(USER_HEADER) Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("Поле с ID должно быть заполнено");
        }

        ItemRequestFullDto[] responseArray = restTemplate.getForObject(SERVER_URL + "/requests", ItemRequestFullDto[].class);

        if (responseArray != null && responseArray.length > 0) {
            return Arrays.stream(responseArray)
                    .map(requestFullDtoMapper::mapToResponseDto)
                    .collect(Collectors.toList());
        }

        log.warn("Ответ от сервера пуст");
        return List.of();
    }



    @GetMapping("/all")
    public List<ItemRequestFullResponseDto> getAllRequestsByPage(@RequestHeader(USER_HEADER) Long userId,
                                                                 @RequestParam(defaultValue = "0") int from,
                                                                 @RequestParam(defaultValue = "10") int size) {
        if (from < 0 || size < 0) {
            throw new IllegalArgumentException("Поля должны быть больше 0");
        }
        if (userId == null) {
            throw new IllegalArgumentException("Поле с ID должно быть заполнено");
        }

        ItemRequestFullDto[] response = restTemplate.getForObject(
                SERVER_URL + "/requests/all?from=" + from + "&size=" + size,
                ItemRequestFullDto[].class
        );

        if (response != null && response.length > 0) {
            return Arrays.stream(response)
                    .map(requestFullDtoMapper::mapToResponseDto)
                    .collect(Collectors.toList());
        }

        return List.of();
    }

    @GetMapping("/{requestId}")
    public ItemRequestFullResponseDto getRequestById(@PathVariable Long requestId) {
        if (requestId == null) {
            throw new IllegalArgumentException("Поле с ID должно быть заполнено");
        }
        ItemRequestFullDto response = restTemplate.getForObject(SERVER_URL + "/requests/" + requestId, ItemRequestFullDto.class);
        if(response!= null){
            return requestFullDtoMapper.mapToResponseDto(response);
        }
        log.warn("Сервер вернул пустой ответ");
        return null;
    }
}