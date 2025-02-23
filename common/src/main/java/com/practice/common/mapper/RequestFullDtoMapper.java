package com.practice.common.mapper;

import com.practice.common.dto.ItemRequestFullDto;
import com.practice.common.dto.ItemRequestFullResponseDto;
import com.practice.common.dto.ItemShortDto;
import java.util.List;
import java.util.stream.Collectors;

public class RequestFullDtoMapper {
    public  ItemRequestFullResponseDto mapToResponseDto(ItemRequestFullDto dto) {
        List<ItemShortDto> mappedItems = dto.getItems().stream()
                .map(item -> ItemShortDto.builder()
                        .id(item.getId())
                        .name(item.getName())
                        .description(item.getDescription())
                        .build())
                .collect(Collectors.toList());

        return ItemRequestFullResponseDto.builder()
                .id(dto.getId())
                .description(dto.getDescription())
                .requesterId(dto.getRequesterId())
                .created(dto.getCreated())
                .items(mappedItems)
                .build();
    }

    public static ItemRequestFullDto toFullDto(ItemRequestFullResponseDto dto) {
        return new ItemRequestFullDto(
                dto.getId(),
                dto.getDescription(),
                dto.getRequesterId(),
                dto.getCreated(),
                dto.getItems()
        );
    }
}
