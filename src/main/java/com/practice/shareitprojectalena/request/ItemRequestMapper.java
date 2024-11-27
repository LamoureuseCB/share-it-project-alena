package com.practice.shareitprojectalena.request;

import com.practice.shareitprojectalena.request.dto.ItemRequestDto;
import com.practice.shareitprojectalena.request.entity.ItemRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ItemRequestMapper {
    public static ItemRequestDto toDto(ItemRequest itemRequest) {
        return ItemRequestDto.builder()
                .id(itemRequest.getId())
                .description(itemRequest.getDescription())
                .requesterId(itemRequest.getRequester().getId())
                .created(itemRequest.getCreated()).build();
    }


public static List<ItemRequestDto> toDto(List<ItemRequest> itemRequests) {
        return itemRequests.stream().map(ItemRequestMapper::toDto).toList();
    }

}
