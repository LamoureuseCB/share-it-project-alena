package com.practice.shareitprojectalena.item.service;

import com.practice.shareitprojectalena.error.exceptions.NotFoundException;
import com.practice.shareitprojectalena.item.entity.Item;
import com.practice.shareitprojectalena.item.repository.ItemRepository;
import com.practice.shareitprojectalena.user.entity.User;
import com.practice.shareitprojectalena.user.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Service
public class ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    public Item create(Item item, Long userId) {

        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь по данному ID не найден"));
        item.setOwner(user);
        return itemRepository.create(item);
    }

    public Item findById(Long id) {
        return itemRepository.findById(id).orElseThrow(() -> new NotFoundException("Вещь для проката по данному ID не найдена"));
    }

    public Item update(Item item, Long itemId, Long userId) {

        return itemRepository.update(item, itemId);
    }

    public List<Item> findAll() {
        return itemRepository.findAll();
    }

    public void delete(Long id) {
        findById(id);
        itemRepository.deleteById(id);
    }

    public List<Item> searchItems(String text) {
        return itemRepository.searchItems(text);
    }
}