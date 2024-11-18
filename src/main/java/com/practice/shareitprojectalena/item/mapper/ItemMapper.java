package com.practice.shareitprojectalena.item.mapper;


import com.practice.shareitprojectalena.item.dto.ItemCreateDto;
import com.practice.shareitprojectalena.item.dto.ItemDto;
import com.practice.shareitprojectalena.item.dto.ItemResponseDto;
import com.practice.shareitprojectalena.item.dto.ItemUpdateDto;
import com.practice.shareitprojectalena.item.entity.Item;

import com.practice.shareitprojectalena.user.entity.User;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class ItemMapper {

    public static ItemDto toDto(Item item) {
        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .isAvailable(item.isAvailable())
//                .ownerId(item.getOwner())
//                .comments()
//                .request(item.getRequest())
                .build();
    }

    public static Item toItem(ItemDto itemDto) {
        return Item.builder()
                .id(itemDto.getId())
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .isAvailable(itemDto.isAvailable())
                .build();
    }

    public static List<ItemDto> toDto(List<Item> items) {
        return items.stream().map(ItemMapper::toDto).toList();
    }

    public static List<Item> toItem(List<ItemDto> itemDtos) {
        return itemDtos.stream().map(ItemMapper::toItem).toList();
    }

    public static Item fromCreate(ItemCreateDto itemCreateDto, User owner) {
        return Item.builder()
                .name(itemCreateDto.getName())
                .description(itemCreateDto.getDescription())
                .isAvailable(itemCreateDto.isAvailable())
                .owner(owner)
                .build();
    }

    public static Item fromUpdate(Item item, ItemUpdateDto itemUpdateDto) {
        if (itemUpdateDto.getName() != null) {
            item.setName(itemUpdateDto.getName());
        }
        if (itemUpdateDto.getDescription() != null) {
            item.setDescription(itemUpdateDto.getDescription());
        }
        if (itemUpdateDto.getIsAvailable() != null) {
            item.setAvailable(itemUpdateDto.getIsAvailable());
        }
        return item;
    }


    public static ItemResponseDto toResponse(Item item) {
        return ItemResponseDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .isAvailable(item.isAvailable())
//                .ownerId(item.getOwner())
//                .requestId(item.getRequest())
                .build();
    }

    public static List<ItemResponseDto> toResponse(List<Item> items) {
        return items.stream().map(ItemMapper::toResponse).toList();
    }
}