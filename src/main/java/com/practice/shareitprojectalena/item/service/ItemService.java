package com.practice.shareitprojectalena.item.service;

import com.practice.shareitprojectalena.error.exceptions.ForbiddenException;
import com.practice.shareitprojectalena.error.exceptions.NotFoundException;
import com.practice.shareitprojectalena.item.entity.Item;
import com.practice.shareitprojectalena.item.mapper.ItemMapper;
import com.practice.shareitprojectalena.item.repository.ItemRepository;
import com.practice.shareitprojectalena.user.entity.User;
import com.practice.shareitprojectalena.user.repository.UserRepository;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;


@AllArgsConstructor
@Service
public class ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ItemMapper itemMapper;

    public Item create(Item item, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь по данному ID не найден"));
        item.setOwner(user);
        return itemRepository.save(item);
    }


    public Item findById(Long id) {
        return itemRepository.findById(id).orElseThrow(() -> new NotFoundException("Вещь для проката по данному ID не найдена"));
    }

    public Item update(Item item, Long itemId, Long userId) {
        Item existingItem = findById(itemId);
        if (!existingItem.getOwner().getId().equals(userId)) {
            throw new ForbiddenException("Обновлять параметры вещи может только владелец");
        }

        itemMapper.merge(existingItem, item);
        return itemRepository.save(existingItem);

    }

    public List<Item> findAll(Long userId) {
        return itemRepository.findAllByOwner_Id(userId);
    }

    public void delete(Long id) {
        Item item = findById(id);
        itemRepository.deleteById(item.getId());
    }

    public List<Item> searchItems(String text) {
        if(text.isBlank()){
            return Collections.emptyList();
        }
        return itemRepository.search(text);
    }

}