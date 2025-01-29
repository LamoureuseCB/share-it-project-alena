package com.practice.shareitprojectalena.controllerTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.practice.shareitprojectalena.error.exceptions.NotFoundException;
import com.practice.shareitprojectalena.item.Item;
import com.practice.shareitprojectalena.item.ItemController;
import com.practice.shareitprojectalena.item.ItemMapper;
import com.practice.shareitprojectalena.item.ItemService;
import com.practice.shareitprojectalena.item.comment.Comment;
import com.practice.shareitprojectalena.item.comment.CommentMapper;
import com.practice.shareitprojectalena.item.comment.CommentService;
import com.practice.shareitprojectalena.item.comment.commentDto.CommentCreateDto;
import com.practice.shareitprojectalena.item.comment.commentDto.CommentResponseDto;
import com.practice.shareitprojectalena.item.itemDto.ItemCreateDto;
import com.practice.shareitprojectalena.item.itemDto.ItemResponseDto;
import com.practice.shareitprojectalena.item.itemDto.ItemUpdateDto;
import com.practice.shareitprojectalena.user.UserService;
import com.practice.shareitprojectalena.user.entity.User;
import jakarta.transaction.Transactional;
import lombok.SneakyThrows;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.practice.shareitprojectalena.utils.RequestConstants.USER_HEADER;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(ItemController.class)

public class ItemControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    ItemService itemService;
    @MockBean
    UserService userService;

    @MockBean
    CommentService commentService;

    @MockBean
    ItemController itemController;

    @MockBean
    private ItemMapper itemMapper;
    @MockBean
    private CommentMapper commentMapper;

    private final Long userId = 1L;

    @Test
    @SneakyThrows
    public void testCreateItemSuccess() {
        ItemCreateDto itemCreateDto = new ItemCreateDto();
        itemCreateDto.setName("Тестовая вещь");
        itemCreateDto.setDescription("Тестовое описание");
        itemCreateDto.setAvailable(true);
        itemCreateDto.setRequestId(null);

        Item item = new Item();
        item.setId(1L);
        item.setName(itemCreateDto.getName());
        item.setDescription(itemCreateDto.getDescription());
        item.setIsAvailable(itemCreateDto.getAvailable());

        when(itemService.create(any(Item.class), eq(userId))).thenReturn(item);

        mockMvc.perform(post("/items")
                        .header(USER_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemCreateDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Тестовая вещь"))
                .andExpect(jsonPath("$.description").value("Тестовое описание"))
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    @SneakyThrows
    public void testCreateItemUserNotFound() {
        ItemCreateDto itemCreateDto = new ItemCreateDto();
        itemCreateDto.setName("Тестовая вещь");
        itemCreateDto.setDescription("Тестовое описание");
        itemCreateDto.setAvailable(true);
        itemCreateDto.setRequestId(null);

        when(itemService.create(any(Item.class), eq(userId))).thenThrow(new NotFoundException("Пользователь по данному ID не найден"));

        mockMvc.perform(post("/items")
                        .header(USER_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemCreateDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Пользователь по данному ID не найден"));
    }

    @Test
    @SneakyThrows
    public void testCreateItemRequestNotFound() {
        ItemCreateDto itemCreateDto = new ItemCreateDto();
        itemCreateDto.setName("Тестовая вещь");
        itemCreateDto.setDescription("Тестовое описание");
        itemCreateDto.setAvailable(true);
        itemCreateDto.setRequestId(null);


        when(itemService.create(any(Item.class), eq(userId))).thenThrow(new NotFoundException("Запрос не найден"));
        mockMvc.perform(post("/items")
                        .header(USER_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemCreateDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Запрос не найден"));
    }

    @Test
    @SneakyThrows
    public void testUpdateItemSuccess() {
        Long itemId = 1L;
        ItemUpdateDto itemUpdateDto = new ItemUpdateDto();
        itemUpdateDto.setName("Обновленная вещь");
        itemUpdateDto.setDescription("Обновленное описание");
        itemUpdateDto.setIsAvailable(false);

        Item updatedItem = new Item();
        updatedItem.setId(itemId);
        updatedItem.setName(itemUpdateDto.getName());
        updatedItem.setDescription(itemUpdateDto.getDescription());
        updatedItem.setIsAvailable(itemUpdateDto.getIsAvailable());

        when(itemService.update(any(Item.class), eq(itemId), eq(userId))).thenReturn(updatedItem);

        mockMvc.perform(patch("/items/{itemId}", itemId)
                        .header(USER_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemUpdateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", Matchers.is(1)))
                .andExpect(jsonPath("$.name", Matchers.is("Обновленная вещь")))
                .andExpect(jsonPath("$.description", Matchers.is("Обновленное описание")))
                .andExpect(jsonPath("$.available", Matchers.is(false)));
    }

    @Test
    @SneakyThrows
    public void testUpdateUnsuccessfully() {
        Long itemId = 99999999L;

        ItemUpdateDto itemUpdateDto = new ItemUpdateDto();
        itemUpdateDto.setName("Вещь с несуществующим ID");
        itemUpdateDto.setDescription("Описание");
        itemUpdateDto.setIsAvailable(false);

        when(itemService.update(any(Item.class), eq(itemId), eq(userId))).thenThrow(new NotFoundException("Объект не найден"));

        mockMvc.perform(patch("/items/{itemId}", itemId)
                        .header(USER_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemUpdateDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Объект не найден"));
    }

    @Test
    @SneakyThrows
    public void testDeleteItemSuccessfully() {
        Long itemId = 1L;

        doNothing().when(itemService).delete(itemId);

        mockMvc.perform(delete("/items/{itemId}", itemId)
                        .header(USER_HEADER, userId))
                .andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    public void testSearchItemsSuccessfully() {

        String text = "само";
        int from = 0;
        int size = 2;


        List<Item> mockItems = new ArrayList<>();
        for (long i = 1; i <= size; i++) {
            Item item = new Item();
            item.setId(i);
            item.setName("Само " + i);
            item.setDescription("Описание " + i);
            item.setIsAvailable(true);
            mockItems.add(item);
        }

        when(itemService.searchItems(text, from, size)).thenReturn(mockItems);

        mockMvc.perform(get("/items/search")
                        .param("text", text)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", Matchers.hasSize(size)))
                .andExpect(jsonPath("$[0].name", Matchers.is("Само 1")))
                .andExpect(jsonPath("$[1].name", Matchers.is("Само 2")));
    }

    @Test
    void findAll_returnsListOfItems() {
        Long userId = 1L;
        int from = 0;
        int size = 10;

        Item item1 = new Item();
        item1.setId(1L);
        item1.setName("Item 1");
        Item item2 = new Item();
        item2.setId(2L);
        item2.setName("Item 2");

        List<Item> items = List.of(item1, item2);
        when(itemService.findAll(userId, from, size)).thenReturn(items);

        ItemResponseDto dto1 = ItemResponseDto.builder()
                .id(1L)
                .name("Item 1")
                .available(true)
                .build();
        ItemResponseDto dto2 = ItemResponseDto.builder()
                .id(2L)
                .name("Item 2")
                .available(true)
                .build();

        List<ItemResponseDto> expectedDtos = List.of(dto1, dto2);

        when(itemMapper.toResponse(items)).thenReturn(expectedDtos);

        List<ItemResponseDto> actualDtos = itemController.findAll(userId, from, size);

        assertEquals(expectedDtos, actualDtos);
    }

    @Test
    void findAll_returnsEmptyList() {
        Long userId = 1L;
        int from = 0;
        int size = 10;
        List<Item> emptyList = Collections.emptyList();
        when(itemService.findAll(userId, from, size)).thenReturn(emptyList);
        when(itemMapper.toResponse(emptyList)).thenReturn(Collections.emptyList());

        List<ItemResponseDto> result = itemController.findAll(userId, from, size);

        assertTrue(result.isEmpty());
    }

    @Test
    void findAll_nullUserIdReturnsBadRequest() {
        Long userId = null;
        int from = 0;
        int size = 10;
        assertThrows(NullPointerException.class, () -> itemController.findAll(userId, from, size));

    }

    @Test
    void create_returnsCreatedComment() {
        Long userId = 1L;
        Long itemId = 2L;
        String text = "Комментарий";

        CommentCreateDto commentCreateDto = new CommentCreateDto();
        commentCreateDto.setText(text);

        User user = new User();
        user.setId(userId);
        when(userService.findById(userId)).thenReturn(user);

        Comment comment = new Comment();
        comment.setId(3);
        comment.setText(text);
        comment.setAuthor(user);
        comment.setCreated(LocalDateTime.now());
        when(commentService.addComment(itemId, user, text)).thenReturn(comment);

        CommentResponseDto expectedResponseDto = CommentResponseDto.builder()
                .id(3)
                .text(text)
                .authorName(user.getName())
                .created(comment.getCreated()).build();
        when(commentMapper.toResponse(comment)).thenReturn(expectedResponseDto);

        CommentResponseDto actualResponseDto = itemController.create(userId, commentCreateDto, itemId);

        assertEquals(expectedResponseDto, actualResponseDto);

    }

    @Test
    void create_userNotFoundThrowsNotFoundException() {
        Long userId = 1L;
        Long itemId = 2L;
        CommentCreateDto commentCreateDto = new CommentCreateDto();
        commentCreateDto.setText("Комментарий от несуществ пользователя");

        when(userService.findById(userId)).thenThrow(new NotFoundException("Пользователь по данному ID не найден"));

        assertThrows(NotFoundException.class, () -> itemController.create(userId, commentCreateDto, itemId));

    }

    @Test
    void create_nullUserIdThrowsException() {
        Long userId = null;
        Long itemId = 2L;
        CommentCreateDto commentCreateDto = new CommentCreateDto();
        commentCreateDto.setText("Комментарий");

        assertThrows(NullPointerException.class, () -> itemController.create(userId, commentCreateDto, itemId));
    }

    @Test
    void getItemWithCommentsSuccess() {
        Long itemId = 1L;
        Item item = new Item();
        item.setId(itemId);
        item.setName("Item1");

        Comment comment1 = new Comment();
        comment1.setId(1);
        comment1.setText("Comment1");
        Comment comment2 = new Comment();
        comment2.setId(2);
        comment2.setText("Comment2");
        List<Comment> comments = List.of(comment1, comment2);

        when(itemService.findById(itemId)).thenReturn(item);
        when(commentService.findByItemId(itemId)).thenReturn(comments);

        CommentResponseDto commentDto1 = CommentResponseDto.builder()
                .id(1)
                .text("Comment1").build();

        CommentResponseDto commentDto2 = CommentResponseDto.builder()
                .id(2)
                .text("Comment2").build();

        ItemResponseDto expectedDto = ItemResponseDto.builder()
                .id(itemId)
                .name("Test item")
                .comments(List.of(commentDto1, commentDto2))
                .build();


        when(itemMapper.toResponseWithComments(item, comments)).thenReturn(expectedDto);

        ItemResponseDto actualDto = itemController.getItemWithComments(itemId);

        assertEquals(expectedDto, actualDto);
    }

    @Test
    void getItemWithComments_itemNotFoundException() {
        Long itemId = 1L;
        when(itemService.findById(itemId)).thenThrow(new NotFoundException("Вещь для проката по данному ID не найдена"));

        assertThrows(NotFoundException.class, () -> itemController.getItemWithComments(itemId));
    }

    @Test
    void getItemWithCommentsReturnsItemWithEmptyComments() {
        Long itemId = 1L;
        Item item = new Item();
        item.setId(itemId);
        item.setName("TestItem");

        when(itemService.findById(itemId)).thenReturn(item);
        when(commentService.findByItemId(itemId)).thenReturn(Collections.emptyList());

        ItemResponseDto expectedDto = ItemResponseDto.builder()
                .id(itemId)
                .name(item.getName())
                .comments(Collections.emptyList()).build();
        when(itemMapper.toResponseWithComments(item, Collections.emptyList())).thenReturn(expectedDto);

        ItemResponseDto actualDto = itemController.getItemWithComments(itemId);

        assertEquals(expectedDto, actualDto);
        assertTrue(actualDto.getComments().isEmpty());
    }

}
