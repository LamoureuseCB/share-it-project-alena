package com.practice.shareitprojectalena.item.service;

import com.practice.shareitprojectalena.error.exceptions.NotFoundException;
import com.practice.shareitprojectalena.item.entity.Item;
import com.practice.shareitprojectalena.item.repository.ItemRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Service
public class ItemService {
    private final ItemRepository itemRepository;

    public Item create(Item item) {
        return itemRepository.create(item);
    }

    public Optional<Item> findById(Long id) {
        return Optional.of(itemRepository.findById(id).orElseThrow(() -> new NotFoundException("Item not found")));
    }

    public Item update(Item item) {
        return itemRepository.update(item);
    }

    public List<Item> findAll() {
        return itemRepository.findAll();
    }

    public void delete(Long id) {
        if (!itemRepository.existsById(id)) {
            throw new NotFoundException("Вещь для проката с ID " + id + " не найдена");
        }
        itemRepository.deleteById(id);
    }

    public List<Item> searchItems(String text) {
        return itemRepository.searchItems(text);
    }
}