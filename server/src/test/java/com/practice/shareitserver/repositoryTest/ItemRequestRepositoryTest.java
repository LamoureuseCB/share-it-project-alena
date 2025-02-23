package com.practice.shareitserver.repositoryTest;

import com.practice.shareitserver.request.ItemRequestRepository;
import com.practice.shareitserver.request.entity.ItemRequest;
import com.practice.shareitserver.user.UserRepository;
import com.practice.shareitserver.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

@DataJpaTest
public class ItemRequestRepositoryTest {
    @Autowired
    private ItemRequestRepository itemRequestRepository;

    @Autowired
    private UserRepository userRepository;

    private User requester;

    @BeforeEach
    void setUp() {
        requester = new User();
        requester.setName("Создатель запроса на бронирование");
        requester.setEmail("requester@request.com");



        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setDescription("описание");
        itemRequest.setRequester(requester);
        itemRequest.setCreated(LocalDateTime.now());

        requester = userRepository.save(requester);
        itemRequestRepository.save(itemRequest);
    }

    @Test
    void findAllByRequesterIdOrderByCreatedDesc_ShouldReturnSortedRequests() {
        Long requesterId = requester.getId();
        Sort sort = Sort.by(Sort.Direction.DESC, "created");
        List<ItemRequest> itemRequests = itemRequestRepository.findAllByRequesterIdOrderByCreatedDesc(requesterId, sort);
        assertFalse(itemRequests.isEmpty());
    }

    @Test
    void findByRequesterId_ShouldReturnPageOfRequests() {
        Long requesterId = requester.getId();
        Pageable pageable = PageRequest.of(0, 10);
        Page<ItemRequest> itemRequests = itemRequestRepository.findByRequesterId(requesterId, pageable);
        assertFalse(itemRequests.isEmpty());
    }
}


