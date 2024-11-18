package com.practice.shareitprojectalena.item.controller;


import com.practice.shareitprojectalena.error.exceptions.NotFoundException;

import com.practice.shareitprojectalena.item.dto.ItemCreateDto;

import com.practice.shareitprojectalena.item.dto.ItemResponseDto;
import com.practice.shareitprojectalena.item.dto.ItemUpdateDto;
import com.practice.shareitprojectalena.item.entity.Item;
import com.practice.shareitprojectalena.item.mapper.ItemMapper;
import com.practice.shareitprojectalena.item.service.ItemService;
import com.practice.shareitprojectalena.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemController {
    private final ItemService itemService;
    private final ItemMapper itemMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemResponseDto create(@RequestHeader User owner, @RequestBody ItemCreateDto itemCreateDto) {
        Item item = itemMapper.fromCreate(itemCreateDto, owner);
        Item createdItem = itemService.create(item);
        return itemMapper.toResponse(createdItem);
    }

    @PutMapping("/{id}")
    public ItemResponseDto update(@PathVariable Long id, @RequestBody ItemUpdateDto itemUpdateDto) {
        Item item = itemService.findById(id)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        item.setName(itemUpdateDto.getName());
        item.setDescription(itemUpdateDto.getDescription());
        Item updatedItem = itemService.update(item);
        return itemMapper.toResponse(updatedItem);
    }

    @GetMapping("/{id}")
    public ItemResponseDto findById(@PathVariable Long id) {
        Item item = itemService.findById(id)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));
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
