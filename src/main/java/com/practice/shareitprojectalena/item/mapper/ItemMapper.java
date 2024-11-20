package com.practice.shareitprojectalena.item.mapper;


import com.practice.shareitprojectalena.item.dto.ItemCreateDto;
import com.practice.shareitprojectalena.item.dto.ItemResponseDto;
import com.practice.shareitprojectalena.item.dto.ItemUpdateDto;
import com.practice.shareitprojectalena.item.entity.Item;

import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class ItemMapper {


    public Item fromCreate(ItemCreateDto itemCreateDto) {
        return Item.builder()
                .name(itemCreateDto.getName())
                .description(itemCreateDto.getDescription())
                .isAvailable(itemCreateDto.getAvailable())
                .build();
    }

    public Item fromUpdate(ItemUpdateDto itemUpdateDto) {
        return Item.builder()
                .name(itemUpdateDto.getName())
                .description(itemUpdateDto.getDescription())
                .isAvailable(itemUpdateDto.getIsAvailable())
                .build();
    }

    public void merge(Item existingItem, Item updatedItem) {
        if (updatedItem.getName() != null && !updatedItem.getName().isBlank()) {
            existingItem.setName(updatedItem.getName());
        }
        if (updatedItem.getDescription() != null && !updatedItem.getDescription().isBlank()) {
            existingItem.setDescription(updatedItem.getDescription());
        }
        if (updatedItem.getIsAvailable() != null) {
            existingItem.setIsAvailable(updatedItem.getIsAvailable());
        }
    }


    public ItemResponseDto toResponse(Item item) {
        return ItemResponseDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getIsAvailable())
                .build();
    }

    public List<ItemResponseDto> toResponse(List<Item> items) {
        return items.stream().map(this::toResponse).toList();
    }
}