package com.practice.shareitprojectalena.item.repository;


import com.practice.shareitprojectalena.item.entity.Item;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemRepository {
    Item create(Item Item);

    Item update(Item item, Long id, Long userId);

    List<Item> findAll(Long userId);

    Optional<Item> findById(Long id);

    List<Item> searchItems(String text);

    void deleteById(Long id);
}
