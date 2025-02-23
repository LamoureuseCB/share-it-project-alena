package com.practice.shareitserver.repositoryTest;

import com.practice.shareitserver.item.Item;
import com.practice.shareitserver.item.ItemRepository;
import com.practice.shareitserver.request.ItemRequestRepository;
import com.practice.shareitserver.request.entity.ItemRequest;
import com.practice.shareitserver.user.UserRepository;
import com.practice.shareitserver.user.entity.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
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
    @Autowired
    private ItemRequestRepository itemRequestRepository;
    private User owner;
    private Item item;
    private ItemRequest request;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setName("Владелец");
        owner.setEmail("owner@owner.com");

        request = new ItemRequest();
        request.setDescription("описание запроса ");

        item = new Item();
        item.setName("Тестовая вещь");
        item.setDescription("описание ");
        item.setIsAvailable(true);

        owner = userRepository.save(owner);

        request.setRequester(owner);
        request = itemRequestRepository.save(request);


        item.setOwner(owner);
        item.setRequest(request);

        List<Item> savedItems = List.of(item);

        for (Item i : savedItems) {
            i.setOwner(owner);
            i.setRequest(request);
            this.item = itemRepository.save(i);
        }

    }

    @Test
    void findAllByOwner_Id() {
        Long ownerId = this.owner.getId();
        List<Item> repositoriesItem = this.itemRepository.findAllByOwner_Id(ownerId, PageRequest.of(0, 10)
        );
        assertEquals(1, repositoriesItem.size());


    }


    @Test
    void search_ByTextAndPage() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Item> items = itemRepository.search("вещь", pageable);

        Assertions.assertFalse(items.isEmpty());
        Assertions.assertTrue(items.get(0).getName().contains(item.getName()));
    }

    @Test
    void findByRequestId() {
        List<Item> items = itemRepository.findByRequestId(request.getId());
        Assertions.assertFalse(items.isEmpty());
        Assertions.assertEquals(1, items.size());
        Assertions.assertEquals(item.getId(), items.get(0).getId());
    }


}

