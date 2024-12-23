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
import lombok.SneakyThrows;
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

    @Spy
    private ItemMapper itemMapper;
    @InjectMocks
    private ItemService itemService;

    private User user;
    private Item item;
    private Long userId = 1L;
    private final Long requestId = 2L;

    @BeforeEach
    public void setUp() {
        user = new User();
        user.setId(userId);
        user.setName("Иван");

        item = new Item();
        item.setName("Дрель");
        item.setIsAvailable(true);
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
    @SneakyThrows
    public void testCreateItemRequestNotFound() {
        User user = new User();
        user.setId(userId);
        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setId(requestId);
        Item item = new Item();
        item.setRequest(itemRequest);
        Mockito.when(itemRequestRepository.findById(requestId)).thenReturn(Optional.empty());
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
        assertEquals("Объект не найден", exception.getMessage());
    }

    @Test
    public void testDeleteItem() {
        Mockito.when(itemRepository.findById(1L))
                .thenReturn(Optional.of(item));
        itemService.delete(1L);
    }

    @Test
    @SneakyThrows
    public void testUpdateItemSuccess() {
        Item updatedItem = new Item();
        updatedItem.setName("Обновленная вещь");
        updatedItem.setIsAvailable(false);

        User owner = new User();
        owner.setId(2L);
        userId = 2L;


        Item item = new Item();
        item.setId(1L);
        item.setOwner(owner);

        Mockito.when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        Mockito.when(itemRepository.save(any(Item.class))).thenReturn(updatedItem);
        Item result = itemService.update(updatedItem, 1L, userId);

        assertNotNull(result);
        assertEquals("Обновленная вещь", result.getName());
        assertFalse(result.getIsAvailable());
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
        User owner = new User();
        owner.setId(2L);
        Item existingItem = new Item();
        existingItem.setId(1L);
        existingItem.setOwner(owner);
        existingItem.setName("Старая вещь");
        existingItem.setIsAvailable(true);
        Item updatedItem = new Item();
        updatedItem.setName("Обновленная вещь");
        updatedItem.setIsAvailable(false);

        Mockito.when(itemRepository.findById(1L))
                .thenReturn(Optional.of(existingItem));
        Mockito.when(itemRepository.save(existingItem)).thenReturn(existingItem);


        Item updated = itemService.update(updatedItem, 1L, 2L);




        assertEquals(updatedItem.getName(), updated.getName());
        assertEquals(updatedItem.getIsAvailable(), updated.getIsAvailable());

    }




    @Test
    @SneakyThrows
    public void failUpdateByAnotherUser_ShouldThrowForbiddenException() {
        Item updatedItem = new Item();
        updatedItem.setName("Обновленная вещь");

        User owner = new User();
        owner.setId(5L);
        Item item = new Item();
        item.setId(1L);
        item.setOwner(owner);
        Long notOwnerId = 1000L;

        Mockito.when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        ForbiddenException exception = assertThrows(ForbiddenException.class, () -> {
            itemService.update(updatedItem, 1L, notOwnerId);
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
        Item notExistItem = new Item();
        item.setId(1L);
        Mockito.when(itemRepository.findById(1L))
                .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            itemService.delete(1L);
        });

        assertEquals("Объект не найден", exception.getMessage());
    }
}

