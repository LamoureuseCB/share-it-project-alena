package com.practice.shareitprojectalena.item.controller;


import com.practice.shareitprojectalena.error.exceptions.NotFoundException;

import com.practice.shareitprojectalena.item.dto.ItemCreateDto;

import com.practice.shareitprojectalena.item.dto.ItemResponseDto;
import com.practice.shareitprojectalena.item.dto.ItemUpdateDto;
import com.practice.shareitprojectalena.item.entity.Item;
import com.practice.shareitprojectalena.item.mapper.ItemMapper;
import com.practice.shareitprojectalena.item.service.ItemService;
import com.practice.shareitprojectalena.user.entity.User;
import com.practice.shareitprojectalena.utils.RequestConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.practice.shareitprojectalena.utils.RequestConstants.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemController {
    private final ItemService itemService;
    private final ItemMapper itemMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemResponseDto create(@RequestHeader(USER_HEADER) Long userId, @RequestBody ItemCreateDto itemCreateDto) {
        Item item = itemMapper.fromCreate(itemCreateDto);
        Item createdItem = itemService.create(item,userId);
        return itemMapper.toResponse(createdItem);
    }

    @PutMapping("/{itemId}")
    public ItemResponseDto update(@RequestHeader(USER_HEADER) Long userId,@PathVariable Long itemId, @RequestBody ItemUpdateDto itemUpdateDto) {
        Item item = itemMapper.fromUpdate(itemUpdateDto);
        Item updatedItem = itemService.update(item,itemId,userId);
        return itemMapper.toResponse(updatedItem);
    }

    @GetMapping("/{id}")
    public ItemResponseDto findById(@PathVariable Long id) {
        Item item = itemService.findById(id);
        return itemMapper.toResponse(item);
    }

    @GetMapping
    public List<ItemResponseDto> findAll() {
        List<Item> items = itemService.findAll();
        return itemMapper.toResponse(items);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        itemService.delete(id);
    }
}
