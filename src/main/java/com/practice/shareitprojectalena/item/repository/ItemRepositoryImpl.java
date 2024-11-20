package com.practice.shareitprojectalena.item.repository;


import com.practice.shareitprojectalena.error.exceptions.NotFoundException;
import com.practice.shareitprojectalena.error.exceptions.ValidationException;
import com.practice.shareitprojectalena.item.entity.Item;

import com.practice.shareitprojectalena.item.mapper.ItemMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
public class ItemRepositoryImpl implements ItemRepository {

    private Map<Long, Item> items = new HashMap<>();
    private Long idCounter = 1L;
    private ItemMapper itemMapper;

    @Override
    public Item create(Item item) {
        validateItem(item);
        item.setId(idCounter++);
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Item update(Item item, Long itemId, Long userId) {
        Item existingItem = items.get(itemId);
        if (existingItem == null) {
            throw new NotFoundException("Вещь с таким ID не найдена");
        }
        if (!existingItem.getOwner().getId().equals(userId)) {
            throw new ValidationException("Обновлять параметры вещи может только владелец");
        }
        validateUpdateItem(item);
        itemMapper.merge(existingItem, item);
        items.put(itemId, existingItem);
        return existingItem;
    }


    @Override
    public List<Item> findAll() {
        return new ArrayList<>(items.values());
    }

    @Override
    public Optional<Item> findById(Long id) {
        return Optional.ofNullable(items.get(id));
    }

    @Override
    public List<Item> searchItems(String text) {
        if (text == null || text.isBlank()) {
            throw new ValidationException("Поле должно быть заполнено");
        }
        return items.values().stream()
                .filter(Item::getIsAvailable)
                .filter(item -> item.getName().toLowerCase().contains(text.toLowerCase())
                        || item.getDescription().toLowerCase().contains(text.toLowerCase()))
                .toList();
    }


    @Override
    public void deleteById(Long id) {
        items.remove(id);

    }

    private void validateItem(Item item) {
        if (item.getName() == null || item.getName().isBlank()) {
            throw new ValidationException("Имя не может быть пустым");
        }

        if (item.getDescription() == null || item.getDescription().isBlank()) {
            throw new ValidationException("Описание не может быть пустым");
        }

        if (item.getIsAvailable() == null) {
            throw new ValidationException("Статус должен быть заполнен");
        }
    }

    private void validateUpdateItem(Item item) {
        if (item.getName() != null && item.getName().isBlank()) {
            throw new ValidationException("Имя не может быть пустым");
        }

        if (item.getDescription() != null && item.getDescription().isBlank()) {
            throw new ValidationException("Описание не может быть пустым");
        }
    }
}
