package com.practice.shareitserver.request;

import com.practice.common.dto.ItemRequestFullDto;
import com.practice.common.dto.ItemShortDto;
import com.practice.shareitserver.item.ItemMapper;
import com.practice.shareitserver.request.dto.ItemRequestDto;
import com.practice.shareitserver.request.entity.ItemRequest;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
@NoArgsConstructor
public class ItemRequestMapper {

    private  ItemMapper itemMapper;

    public ItemRequestDto toItemRequestDto(ItemRequest itemRequest) {
        if (itemRequest == null) {
            return null;
        }

        return ItemRequestDto.builder()
                .id(itemRequest.getId())
                .requesterId(itemRequest.getRequester() != null ? itemRequest.getRequester().getId() : null)
                .created(itemRequest.getCreated())
                .description(itemRequest.getDescription())
                .build();
    }

    public ItemRequestFullDto toItemRequestFullDto(ItemRequest itemRequest) {
        if (itemRequest == null) {
            return null;
        }

        return ItemRequestFullDto.builder()
                .id(itemRequest.getId())
                .description(itemRequest.getDescription())
                .requesterId(itemRequest.getRequester() != null ? itemRequest.getRequester().getId() : null)
                .created(itemRequest.getCreated())
                .items(mapItemsToShortDto(itemRequest))
                .build();
    }

    private List<ItemShortDto> mapItemsToShortDto(ItemRequest itemRequest) {
        if (itemRequest.getItems() == null || itemRequest.getItems().isEmpty()) {
            return Collections.emptyList();
        }

        List<ItemShortDto> collect = itemRequest.getItems().stream()
                .map(itemMapper::toItemShortDto)
                .collect(Collectors.toList());
        return collect;
    }
}
