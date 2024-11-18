package com.practice.shareitprojectalena.item.repository;

import com.practice.shareitprojectalena.item.dto.ItemDto;
import com.practice.shareitprojectalena.item.entity.Item;
import com.practice.shareitprojectalena.request.entity.ItemRequest;
import com.practice.shareitprojectalena.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    Item create(Item Item);

    Item update(Item item);

    List<Item> findAll();

    Optional<Item> findById(Long id);
//
//    Optional<Item> findByOwner_Id(Long ownerId);
List<Item> searchItems(String text);

    void deleteById(Long id);
}
