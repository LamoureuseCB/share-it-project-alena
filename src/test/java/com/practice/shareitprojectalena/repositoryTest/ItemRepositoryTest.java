package com.practice.shareitprojectalena.repositoryTest;

import com.practice.shareitprojectalena.item.Item;
import com.practice.shareitprojectalena.item.ItemRepository;
import com.practice.shareitprojectalena.user.UserRepository;
import com.practice.shareitprojectalena.user.entity.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
public class ItemRepositoryTest {
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private UserRepository userRepository;


    @Test
    void findAllByOwner_Id() {
        List<Item> items = itemRepository.findAllByOwner_Id(1L, PageRequest.of(0, 10));
        assertEquals(10, items.size());
    }

    @Test
    void searchAByTextAndPage() {
        User owner = User.builder()
                .name("user1")
                .email("user1@email.com")
                .build();
        owner = userRepository.save(owner);
        Item item = Item.builder()
                .name("name")
                .description("description")
                .isAvailable(true)
                .owner(owner)
                .build();
        item = itemRepository.save(item);
        Pageable pageable = PageRequest.of(0, 10);
        List<Item> items = itemRepository.search("name", pageable);
//        Assertions.assertTrue(items.getFirst().getName().contains(item.getName()));
//    }

    }
}
