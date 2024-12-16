package com.practice.shareitprojectalena.serviceTest;

import com.practice.shareitprojectalena.error.exceptions.ForbiddenException;
import com.practice.shareitprojectalena.error.exceptions.NotFoundException;
import com.practice.shareitprojectalena.item.Item;
import com.practice.shareitprojectalena.item.ItemRepository;
import com.practice.shareitprojectalena.item.ItemService;
import com.practice.shareitprojectalena.request.ItemRequestRepository;
import com.practice.shareitprojectalena.user.UserRepository;
import com.practice.shareitprojectalena.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

public class ItemServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRequestRepository itemRequestRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemService itemService;


    private User user;
    private Item item;
    private final Long userId = 1L;
    private final Long requestId = 2L;

    @BeforeEach
    public void setUp() {
        user = new User();
        user.setId(userId);
        user.setName("Test User");

        item = new Item();
        item.setName("Test Item");
        item.setIsAvailable(true);
        item.setRequestId(requestId);
    }

    @Test
    public void testCreateItemSuccess() {
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));
        when(itemRepository.save(any(Item.class)))
                .thenReturn(item);

        Item createdItem = itemService.create(item, userId);

        assertNotNull(createdItem);
        assertEquals(item.getName(), createdItem.getName());
        assertEquals(user, createdItem.getOwner());
    }

    @Test
    public void testCreateItemUserNotFound() {
        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            itemService.create(item, userId);
        });

        assertEquals("Пользователь по данному ID не найден", exception.getMessage());
    }

    @Test
    public void testCreateItemRequestNotFound() {
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));
        when(itemRequestRepository.findById(requestId))
                .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            itemService.create(item, userId);
        });

        assertEquals("Запрос не найден", exception.getMessage());

    }

    @Test
    public void testFindByIdSuccess() {
        when(itemRepository.findById(1L))
                .thenReturn(Optional.of(item));

        Item foundItem = itemService.findById(1L);

        assertNotNull(foundItem);
        assertEquals(item.getName(), foundItem.getName());
        assertEquals(item.getOwner(), foundItem.getOwner());
    }

    @Test
    public void testFindByIdShouldThrowNotFound() {
        when(itemRepository.findById(1L))
                .thenReturn(Optional.empty());
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            itemService.findById(1L);
        });
        assertEquals("Вещь для проката по данному ID не найдена", exception.getMessage());
    }

    @Test
    public void testDeleteItem() {
        when(itemRepository.findById(1L))
                .thenReturn(Optional.of(item));
        itemService.delete(1L);
    }

    @Test
    public void testUpdateItemSuccess() {
        Item updatedItem = new Item();
        updatedItem.setName("Обновленная вещь");
        updatedItem.setIsAvailable(false);

        when(itemRepository.findById(1L))
                .thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class)))
                .thenReturn(updatedItem);
        Item result = itemService.update(updatedItem, 1L, userId);
        assertNotNull(result);
        assertEquals("Обновленная вещь", result.getName());
        assertFalse(result.getIsAvailable());
    }

    @Test
    public void testFailUpdateItemByAnotherUserForbidden() {
        Item updatedItem = new Item();
        updatedItem.setName("Обновленная вещь");

        when(itemRepository.findById(1L))
                .thenReturn(Optional.of(item));
        when(item.getOwner().getId()).thenReturn(2L);

        ForbiddenException exception = assertThrows(ForbiddenException.class, () -> {
            itemService.update(updatedItem, 1L, userId);
        });
        assertEquals("Обновлять параметры вещи может только владелец", exception.getMessage());
    }
}

