package com.practice.shareitprojectalena.request;
import com.practice.shareitprojectalena.request.dto.ItemRequestDto;
import com.practice.shareitprojectalena.request.dto.ItemRequestFullDto;
import com.practice.shareitprojectalena.request.entity.ItemRequest;

import org.springframework.stereotype.Component;


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



    public ItemRequestFullDto toItemRequestFullDto(ItemRequest itemRequest) {
        return ItemRequestFullDto.builder()
                .id(itemRequest.getId())
                .description(itemRequest.getDescription())
                .requesterId(itemRequest.getRequester().getId())
                .created(itemRequest.getCreated())
                .items(itemRequest.getItems())
                .build();
    }

}
