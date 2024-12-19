package com.practice.shareitprojectalena.serviceTest;

import com.practice.shareitprojectalena.error.exceptions.ForbiddenException;
import com.practice.shareitprojectalena.error.exceptions.NotFoundException;
import com.practice.shareitprojectalena.item.Item;
import com.practice.shareitprojectalena.item.ItemMapper;
import com.practice.shareitprojectalena.item.ItemRepository;
import com.practice.shareitprojectalena.item.ItemService;
import com.practice.shareitprojectalena.request.ItemRequestRepository;
import com.practice.shareitprojectalena.request.entity.ItemRequest;
import com.practice.shareitprojectalena.user.UserRepository;
import com.practice.shareitprojectalena.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.any;

@ExtendWith(MockitoExtension.class)
public class ItemServiceTest {
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRequestRepository itemRequestRepository;

    @InjectMocks
    private ItemService itemService;
    @Spy
    private ItemMapper itemMapper;


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
        item.setOwner(user);
    }

    @Test
    public void testCreateItemSuccess() {
        Mockito.when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));
        Mockito.when(itemRepository.save(any(Item.class)))
                .thenReturn(item);

        Item createdItem = itemService.create(item, userId);

        assertNotNull(createdItem);
        assertEquals(item.getName(), createdItem.getName());
        assertEquals(user, createdItem.getOwner());
    }

    @Test
    public void testCreateItemUserNotFound() {
        Mockito.when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            itemService.create(item, userId);
        });

        assertEquals("Пользователь по данному ID не найден", exception.getMessage());
    }

    @Test
    public void testCreateItemRequestNotFound() {
        Mockito.when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));
        Mockito.when(itemRequestRepository.findById(requestId))
                .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            itemService.create(item, userId);
        });

        assertEquals("Запрос не найден", exception.getMessage());

    }

    @Test
    public void testFindByIdSuccess() {
        Mockito.when(itemRepository.findById(1L))
                .thenReturn(Optional.of(item));

        Item foundItem = itemService.findById(1L);

        assertNotNull(foundItem);
        assertEquals(item.getName(), foundItem.getName());
        assertEquals(item.getOwner(), foundItem.getOwner());
    }

    @Test
    public void testFindByIdShouldThrowNotFound() {
        Mockito.when(itemRepository.findById(1L))
                .thenReturn(Optional.empty());
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            itemService.findById(1L);
        });
        assertEquals("Вещь для проката по данному ID не найдена", exception.getMessage());
    }

    @Test
    public void testDeleteItem() {
        Mockito.when(itemRepository.findById(1L))
                .thenReturn(Optional.of(item));
        itemService.delete(1L);
    }

    @Test
    public void testUpdateItemSuccess() {
        Item updatedItem = new Item();
        updatedItem.setName("Обновленная вещь");
        updatedItem.setIsAvailable(false);

        Mockito.when(itemRepository.findById(1L))
                .thenReturn(Optional.of(item));
        Mockito.when(itemRepository.save(any(Item.class)))
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

        Mockito.when(itemRepository.findById(1L))
                .thenReturn(Optional.of(item));
        Mockito.when(item.getOwner().getId()).thenReturn(2L);

        ForbiddenException exception = assertThrows(ForbiddenException.class, () -> {
            itemService.update(updatedItem, 1L, userId);
        });
        assertEquals("Обновлять параметры вещи может только владелец", exception.getMessage());
    }

    @Test
    void CreateItemWithExistingUserAndRequest() {
        Item item = new Item();
        item.setRequest(new ItemRequest());
        item.getRequest().setId(1L);

        Mockito.when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));
        Mockito.when(itemRequestRepository.findById(1L))
                .thenReturn(Optional.of(item.getRequest()));
        Mockito.when(itemRepository.save(any(Item.class)))
                .thenReturn(item);

        Item createdItem = itemService.create(item, userId);

        assertNotNull(createdItem);
        assertEquals(user, createdItem.getOwner());
        assertEquals(item.getRequest(), createdItem.getRequest());
    }

    @Test
    void create_ItemUserNotFound() {
        Mockito.when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            itemService.create(new Item(), userId);
        });

        assertEquals("Пользователь по данному ID не найден", exception.getMessage());
    }

    @Test
    void create_ItemRequestNotFound() {
        Item item = new Item();
        item.setRequest(new ItemRequest());
        item.getRequest().setId(1L);

        Mockito.when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));
        Mockito.when(itemRequestRepository.findById(1L))
                .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            itemService.create(item, userId);
        });

        assertEquals("Запрос не найден", exception.getMessage());
    }

    @Test
    void update_ItemSuccess() {
        Item updatedItem = new Item();
        updatedItem.setName("обновленная вещь");

        Mockito.when(itemRepository.findById(1L))
                .thenReturn(Optional.of(item));
        Mockito.when(item.getOwner().getId()).thenReturn(userId);

        Item updated = itemService.update(updatedItem, 1L, userId);

        assertEquals(updatedItem.getName(), updated.getName());
    }

    @Test
    void update_ItemForbidden() {
        Item updatedItem = new Item();
        updatedItem.setName("обновленная вещь");

        Mockito.when(itemRepository.findById(1L))
                .thenReturn(Optional.of(item));
        Mockito.when(item.getOwner().getId()).thenReturn(2L);

        ForbiddenException exception = assertThrows(ForbiddenException.class, () -> {
            itemService.update(updatedItem, 1L, userId);
        });

        assertEquals("Обновлять параметры вещи может только владелец", exception.getMessage());
    }


    @Test
    void delete_ItemSuccess() {
        Item item = new Item();
        item.setId(1L);

        Mockito.when(itemRepository.findById(1L))
                .thenReturn(Optional.of(item));

        itemService.delete(1L);
    }

    @Test
    void delete_NotFound() {
        Mockito.when(itemRepository.findById(1L))
                .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            itemService.delete(1L);
        });

        assertEquals("Объект не найден", exception.getMessage());
    }
}

