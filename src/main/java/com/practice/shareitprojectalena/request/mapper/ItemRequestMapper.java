package com.practice.shareitprojectalena.request.mapper;

import com.practice.shareitprojectalena.request.dto.ItemRequestDto;
import com.practice.shareitprojectalena.request.entity.ItemRequest;
import com.practice.shareitprojectalena.user.entity.User;
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


//    public static ItemRequest toItemRequest(ItemRequestDto itemRequestDto) {
//        return ItemRequest.builder()
//                .id(itemRequestDto.getId())
//                .description(itemRequestDto.getDescription())
//                .requester()
//                .created(itemRequestDto.getCreated())
//                .build();
//    }
    public static List<ItemRequestDto> toDto(List<ItemRequest> itemRequests){
        return itemRequests.stream().map(ItemRequestMapper::toDto).toList();
    }

//    public static List<ItemRequest> toItemRequest(List<ItemRequestDto> itemRequestsDtos) {
//        return itemRequestsDtos.stream()
//                .map(ItemRequestMapper::toItemRequest)
//                .toList();
//    }

}
