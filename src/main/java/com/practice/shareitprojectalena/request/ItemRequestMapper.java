package com.practice.shareitprojectalena.request;

import com.practice.shareitprojectalena.item.Item;
import com.practice.shareitprojectalena.item.itemDto.ItemShortResponseDto;
import com.practice.shareitprojectalena.request.dto.ItemRequestCreateDto;
import com.practice.shareitprojectalena.request.dto.ItemRequestDto;
import com.practice.shareitprojectalena.request.dto.ItemRequestFullDto;
import com.practice.shareitprojectalena.request.entity.ItemRequest;
import com.practice.shareitprojectalena.user.entity.User;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ItemRequestMapper {

    public ItemRequestDto toItemRequestDto(ItemRequest itemRequest) {
        return ItemRequestDto.builder()
                .id(itemRequest.getId())
                .requesterId(itemRequest.getRequester().getId())
                .created(itemRequest.getCreated())
                .description(itemRequest.getDescription())
                .build();
    }

    public ItemRequest toItemRequest(ItemRequestCreateDto itemRequestCreateDto, User user) {
        return ItemRequest.builder()
                .requester(user)
                .created(LocalDateTime.now())
                .description(itemRequestCreateDto.getDescription())
                .build();
    }

    public ItemRequestFullDto toItemRequestFullDto(ItemRequest itemRequest) {
        return ItemRequestFullDto.builder()
                .id(itemRequest.getId())
                .description(itemRequest.getDescription())
                .requesterId(itemRequest.getRequester().getId())
                .created(itemRequest.getCreated())
                .items(itemRequest.getItems())
                .build();
    }

    public List<ItemShortResponseDto> toItemShortResponseDtos(List<Item> items) {
        return items.stream()
                .map(this::toItemShortResponseDto)
                .collect(Collectors.toList());
    }

    public ItemShortResponseDto toItemShortResponseDto(Item item) {
        return ItemShortResponseDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getIsAvailable())
                .build();
    }
}
