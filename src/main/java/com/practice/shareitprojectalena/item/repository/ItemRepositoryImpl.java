package com.practice.shareitprojectalena.item.repository;

import com.practice.shareitprojectalena.error.exceptions.NotFoundException;
import com.practice.shareitprojectalena.item.entity.Item;
import com.practice.shareitprojectalena.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
public abstract class ItemRepositoryImpl implements ItemRepository {

    private Map<Long, Item> items = new HashMap<>();
    private Long idCounter = 0L;

    @Override
    public Item create(Item item) {
        item.setId(idCounter++);
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Item update(Item item) {
        if (!items.containsKey(item.getId())) {
            throw new NotFoundException("Вещь для проката  с ID " + item.getId() + " не найден");
        }
        items.put(item.getId(), item);
        return item;
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
            throw new RuntimeException("Поле должно быть заполнено");
        }
        return items.values().stream()
                .filter(item -> item.getName().toLowerCase().contains(text.toLowerCase()) || item.getDescription().toLowerCase().contains(text.toLowerCase()))
                .toList();
    }


    @Override
    public void deleteById(Long id) {
        if (!items.containsKey(id)) {
            throw new NotFoundException("Вещь для проката с ID " + id + " не найдена");
        }
        items.remove(id);

    }
}
