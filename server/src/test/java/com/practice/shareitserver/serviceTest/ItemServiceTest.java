package com.practice.shareitserver.serviceTest;


import com.practice.shareitserver.error.exceptions.ForbiddenException;
import com.practice.shareitserver.error.exceptions.InvalidPageException;
import com.practice.shareitserver.error.exceptions.InvalidSizeException;
import com.practice.shareitserver.error.exceptions.NotFoundException;
import com.practice.shareitserver.item.Item;
import com.practice.shareitserver.item.ItemMapper;
import com.practice.shareitserver.item.ItemRepository;
import com.practice.shareitserver.item.ItemService;
import com.practice.shareitserver.request.ItemRequestRepository;
import com.practice.shareitserver.request.entity.ItemRequest;
import com.practice.shareitserver.user.UserRepository;
import com.practice.shareitserver.user.entity.User;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

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
    private Long itemId = 1L;

    @BeforeEach
    public void setUp() {
        user = new User();
        user.setId(userId);
        user.setName("Иван");

        item = new Item();
        item.setName("Дрель");
        item.setId(itemId);
        item.setIsAvailable(true);
  }

    @Test
    public void testCreateItemSuccess() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
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
    @SneakyThrows
    public void testCreateItemRequestNotFound() {
        User user = new User();
        user.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        ItemRequest itemRequest = new ItemRequest();
        Long requestId = 2L;
        itemRequest.setId(requestId);
        Item item = new Item();
        item.setRequest(itemRequest);
        when(itemRequestRepository.findById(requestId)).thenReturn(Optional.empty());
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
        assertEquals("Объект не найден", exception.getMessage());
    }

    @Test
    public void testDeleteItem() {
        when(itemRepository.findById(1L))
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

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class))).thenReturn(updatedItem);
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

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));
        when(itemRequestRepository.findById(1L))
                .thenReturn(Optional.of(item.getRequest()));
        when(itemRepository.save(any(Item.class)))
                .thenReturn(item);

        Item createdItem = itemService.create(item, userId);

        assertNotNull(createdItem);
        assertEquals(user, createdItem.getOwner());
        assertEquals(item.getRequest(), createdItem.getRequest());
    }

    @Test
    void create_ItemUserNotFound() {
        when(userRepository.findById(userId))
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

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));
        when(itemRequestRepository.findById(1L))
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

        when(itemRepository.findById(1L))
                .thenReturn(Optional.of(existingItem));
        when(itemRepository.save(existingItem)).thenReturn(existingItem);


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

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        ForbiddenException exception = assertThrows(ForbiddenException.class, () -> {
            itemService.update(updatedItem, 1L, notOwnerId);
        });

        assertEquals("Обновлять параметры вещи может только владелец", exception.getMessage());
    }



    @Test
    void findAll_ValidParameters_ReturnsItemList() throws InvalidSizeException, InvalidPageException {

        Long userId = 1L;
        int from = 0;
        int size = 10;

        Item item1 = new Item();
        Item item2 = new Item();

        when(itemRepository.findAllByOwner_Id(userId, PageRequest.of(from / size, size)))
                .thenReturn(List.of(item1, item2));


        List<Item> result = itemService.findAll(userId, from, size);


        assertEquals(2, result.size(), "Expected to find two items");
        assertTrue(result.contains(item1));
        assertTrue(result.contains(item2));
    }


        @Test
        void delete_ItemSuccess () {
            Item item = new Item();
            item.setId(1L);

            when(itemRepository.findById(1L))
                    .thenReturn(Optional.of(item));

            itemService.delete(1L);
        }

        @Test
        void delete_NotFound () {
            Item notExistItem = new Item();
            item.setId(1L);
            when(itemRepository.findById(1L))
                    .thenReturn(Optional.empty());

            NotFoundException exception = assertThrows(NotFoundException.class, () -> {
                itemService.delete(1L);
            });

            assertEquals("Объект не найден", exception.getMessage());
        }

        @Test
        void searchItems_EmptyText_ReturnsEmptyList () throws InvalidSizeException, InvalidPageException {
            String text = "";
            int from = 0;
            int size = 10;

            List<Item> result = itemService.searchItems(text, from, size);

            assertTrue(result.isEmpty(), "");
        }
    @Test
    void searchItems_ValidParameters_ReturnsItemList () throws InvalidSizeException, InvalidPageException {

        String text = "item";
        int from = 0;
        int size = 10;

        Item item1 = new Item();
        Item item2 = new Item();

        when(itemRepository.search(text, PageRequest.of(from, size)))
                .thenReturn(List.of(item1, item2));

        List<Item> result = itemService.searchItems(text, from, size);

        assertEquals(2, result.size(), "Ожидается 2 предмета");
        assertTrue(result.contains(item1));
        assertTrue(result.contains(item2));
    }

        @Test
        void searchItems_NegativeFrom_ThrowsInvalidPageException () {
            String text = "item";
            int from = -1;
            int size = 10;

            InvalidPageException exception = assertThrows(InvalidPageException.class, () -> {
                itemService.searchItems(text, from, size);
            });
            assertEquals("Ошибка!Страница не должна быть меньше нуля", exception.getMessage());
        }

    @Test
    void searchItems_NonPositiveSize_ThrowsInvalidSizeException() {
        String text = "item";
        int from = 0;
        int size = 0;

        InvalidSizeException exception = assertThrows(InvalidSizeException.class, () -> {
            itemService.searchItems(text, from, size);
        });
        assertEquals("Ошибка!Размер должен быть положительным", exception.getMessage());
    }



    @Test
    void findAll_NegativeFrom_ThrowsInvalidPageException() {
        int from = -1;
        int size = 10;
        InvalidPageException exception = assertThrows(InvalidPageException.class, () -> itemService.findAll(userId, from, size));
        Assertions.assertEquals("Ошибка!Страница не должна быть меньше нуля", exception.getMessage());
    }

    @Test
    void findAll_NonPositiveSize_ThrowsInvalidSizeException() {
        Long userId = 1L;
        int from = 0;
        int size = 0;
        InvalidSizeException exception = assertThrows(InvalidSizeException.class, () -> itemService.findAll(userId, from, size));

        Assertions.assertEquals("Ошибка!Размер должен быть положительным", exception.getMessage());
    }
    }



