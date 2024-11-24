package com.practice.shareitprojectalena.item;


import com.practice.shareitprojectalena.item.comment.Comment;
import com.practice.shareitprojectalena.item.comment.CommentMapper;
import com.practice.shareitprojectalena.item.itemDto.ItemCreateDto;
import com.practice.shareitprojectalena.item.itemDto.ItemResponseDto;
import com.practice.shareitprojectalena.item.itemDto.ItemUpdateDto;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
@RequiredArgsConstructor
public class ItemMapper {
    private final CommentMapper commentMapper;



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
    public ItemResponseDto toResponseWithComments(Item item, List<Comment> comments) {
        return ItemResponseDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getIsAvailable())
                .comments(comments)
                .build();

    }

}