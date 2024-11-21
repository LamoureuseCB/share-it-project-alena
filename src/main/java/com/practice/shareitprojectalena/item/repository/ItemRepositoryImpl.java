package com.practice.shareitprojectalena.item.repository;



import com.practice.shareitprojectalena.error.exceptions.ValidationException;
import com.practice.shareitprojectalena.item.entity.Item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
public class ItemRepositoryImpl implements ItemRepository {

    private Map<Long, Item> items = new HashMap<>();
    private Long idCounter = 1L;


    @Override
    public Item create(Item item) {

        item.setId(idCounter++);
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Item update(Item item, Long itemId, Long userId) {
        Item existingItem = items.get(itemId);

        items.put(itemId, existingItem);
        return existingItem;
    }


    @Override
    public List<Item> findAll(Long userId) {
        return items.values().stream().filter(item -> item.getOwner().getId().equals(userId)).toList();
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
}
