package com.practice.shareitserver.controllerTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.practice.shareitserver.error.exceptions.NotFoundException;
import com.practice.shareitserver.item.Item;
import com.practice.shareitserver.item.ItemController;
import com.practice.shareitserver.item.ItemMapper;
import com.practice.shareitserver.item.ItemService;
import com.practice.shareitserver.item.comment.Comment;
import com.practice.shareitserver.item.comment.CommentMapper;
import com.practice.shareitserver.item.comment.CommentService;
import com.practice.shareitserver.item.comment.commentDto.CommentCreateDto;
import com.practice.shareitserver.item.comment.commentDto.CommentResponseDto;
import com.practice.shareitserver.item.itemDto.ItemCreateDto;
import com.practice.shareitserver.item.itemDto.ItemResponseDto;
import com.practice.shareitserver.item.itemDto.ItemUpdateDto;
import com.practice.shareitserver.user.UserService;
import com.practice.shareitserver.user.entity.User;
import lombok.SneakyThrows;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.practice.shareitserver.utils.RequestConstants.USER_HEADER;
import static org.mockito.ArgumentMatchers.*;
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
    @SpyBean
    private ItemMapper itemMapper;
    @SpyBean
    private CommentMapper commentMapper;

    private final Long userId = 1L;

    private User booker;
    private User owner;
    private final Long bookerId = 2L;
    private Item item;
    private final Long itemId = 1L;

    private final int from = 0;
    private final int size = 10;

    @BeforeEach
    void setUp() {

        owner = new User();
        owner.setId(userId);
        owner.setName("Тест владелец");
        owner.setEmail("owner@o.com");

        booker = new User();
        booker.setId(bookerId);
        booker.setName("Test Booker");
        booker.setEmail("booker@b.com");


        item = new Item();
        item.setId(itemId);
        item.setName("Тестовая вещь");
        item.setDescription("Тестовое описание");
        item.setIsAvailable(true);

        owner.setItems(List.of(item));

    }


    private ItemCreateDto createItemDto() {
        ItemCreateDto itemCreateDto = new ItemCreateDto();
        itemCreateDto.setName("Тестовая вещь");
        itemCreateDto.setDescription("Тестовое описание");
        itemCreateDto.setAvailable(true);
        return itemCreateDto;
    }

    private Item createMockItem(ItemCreateDto itemCreateDto) {
        Item mockItem = new Item();
        mockItem.setId(1L);
        mockItem.setName(itemCreateDto.getName());
        mockItem.setDescription(itemCreateDto.getDescription());
        mockItem.setIsAvailable(itemCreateDto.getAvailable());
        mockItem.setRequest(null);
        return mockItem;
    }


    private ItemResponseDto createItemResponseDto(Item item) {
        if (item == null) {
            throw new IllegalArgumentException("Поле с предметом должно быть заполнено");
        }

        return new ItemResponseDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getIsAvailable(),
                new ArrayList<>(),
                null,
                null
        );
    }

    private ItemResponseDto toResponseWithComments(Item item, List<Comment> comments) {
        return ItemResponseDto.builder()
                .id(item.getId())
                .name(item.getName())
                .available(item.getIsAvailable())
                .comments(commentMapper.toResponse(comments))
                .build();

    }

    @Test
    @SneakyThrows
    public void create_ItemSuccess() {
        ItemCreateDto itemCreateDto = createItemDto();
        Item mockingItem = createMockItem(itemCreateDto);
        ItemResponseDto itemResponseDto = createItemResponseDto(mockingItem);

        doReturn(mockingItem).when(itemMapper).fromCreate(any(ItemCreateDto.class));
        when(itemService.create(any(Item.class), eq(userId))).thenReturn(mockingItem);
        doReturn(itemResponseDto).when(itemMapper).toResponse(any(Item.class));

        mockMvc.perform(post("/items")
                        .header(USER_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemCreateDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(itemId))
                .andExpect(jsonPath("$.name").value("Тестовая вещь"))
                .andExpect(jsonPath("$.description").value("Тестовое описание"))
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    @SneakyThrows
    public void create_Item_UserNotFound() {
        ItemCreateDto itemCreateDto = createItemDto();
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
        ItemCreateDto itemCreateDto = createItemDto();

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
    void updateItem() {
        String newDescription = "Обновленное описание";

        ItemUpdateDto itemUpdateDto = new ItemUpdateDto();
        itemUpdateDto.setName(item.getName());
        itemUpdateDto.setDescription(newDescription);
        itemUpdateDto.setIsAvailable(true);


        Item updatedItem = new Item();
        updatedItem.setId(itemId);
        updatedItem.setName(itemUpdateDto.getName());
        updatedItem.setDescription(itemUpdateDto.getDescription());
        updatedItem.setOwner(owner);
        updatedItem.setIsAvailable(true);

        ItemResponseDto itemResponseDto = createItemResponseDto(updatedItem);

        doReturn(updatedItem).when(itemMapper).fromUpdate(itemUpdateDto);
        when(itemService.update(any(Item.class), eq(itemId), eq(userId))).thenReturn(updatedItem);
        doReturn(itemResponseDto).when(itemMapper).toResponse(updatedItem);


        mockMvc.perform(MockMvcRequestBuilders.patch("/items/{itemId}", itemId)
                        .header(USER_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemUpdateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemId))
                .andExpect(jsonPath("$.name").value("Тестовая вещь"))
                .andExpect(jsonPath("$.description").value("Обновленное описание"))
                .andReturn()
                .getResponse()
                .getContentAsString();


    }


    @Test
    @SneakyThrows
    public void testUpdateUnsuccessfully() {

        Long itemId = 99999L;
        Long userId = 10L;
        ItemUpdateDto itemUpdateDto = new ItemUpdateDto("Новая вещь", "Новое описание", true);


        when(itemService.update(any(Item.class), eq(itemId), eq(userId)))
                .thenThrow(new NotFoundException("Объект не найден"));

        mockMvc.perform(MockMvcRequestBuilders.patch("/items/{itemId}", itemId)
                        .header(USER_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemUpdateDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Объект не найден"));
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
    public void shouldReturnNotFound_WhenItemIsNotAvailable() {

        Long unavailableItemId = 99999999L;

        when(itemService.findById(unavailableItemId))
                .thenThrow(new NotFoundException("Вещь не найдена"));

        mockMvc.perform(get("/items/{itemId}", unavailableItemId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Вещь не найдена"));
    }


    @Test
    @SneakyThrows
    public void searchItems_Successfully() {
        String text = "Тест";
        int from = 0;
        int size = 10;


        List<Item> mockItems = new ArrayList<>();
        for (long i = 1; i <= size; i++) {
            Item item = new Item();
            item.setId(i);
            item.setName("Тестовая вещь " + i);
            item.setDescription("Описание тестовой вещи " + i);
            item.setIsAvailable(true);
            mockItems.add(item);
        }


        List<ItemResponseDto> responseDtos = new ArrayList<>();
        for (Item mockItem : mockItems) {
            ItemResponseDto itemResponseDto = new ItemResponseDto();
            itemResponseDto.setId(mockItem.getId());
            itemResponseDto.setName(mockItem.getName());
            itemResponseDto.setDescription(mockItem.getDescription());
            itemResponseDto.setAvailable(mockItem.getIsAvailable());
            responseDtos.add(itemResponseDto);
        }


        when(itemService.searchItems(text, from, size)).thenReturn(mockItems);
        when(itemMapper.toResponse(mockItems)).thenReturn(responseDtos);

        mockMvc.perform(get("/items/search")
                        .param("text", text)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", Matchers.hasSize(size)))
                .andExpect(jsonPath("$[0].name", Matchers.is("Тестовая вещь 1")))
                .andExpect(jsonPath("$[1].name", Matchers.is("Тестовая вещь 2")));
    }

    @Test
    @SneakyThrows
    public void searchItems_EmptyResult() {
        when(itemService.searchItems(anyString(), anyInt(), anyInt())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/items/search")
                        .param("text", "не существует")
                        .param("from", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", Matchers.hasSize(0)));
    }


    @Test
    @SneakyThrows
    void findAll_returnsListOfItems() {
        ItemResponseDto itemResponseDto1 = createItemResponseDto(item);
        Item item2 = new Item();
        item2.setId(2L);
        item2.setName("Тестовая вещь2");
        item2.setIsAvailable(true);
        List<Item> items = List.of(item, item2);
        ItemResponseDto itemResponseDto2 = createItemResponseDto(item2);
        List<ItemResponseDto> responseDtos = List.of(itemResponseDto1, itemResponseDto2);


        when(itemService.findAll(eq(userId), eq(from), eq(size))).thenReturn(items);
        when(itemMapper.toResponse(items)).thenReturn(responseDtos);

        mockMvc.perform(get("/items")
                        .header(USER_HEADER, userId)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size))
                        .contentType(MediaType.APPLICATION_JSON))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(responseDtos.size()))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Тестовая вещь"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].name").value("Тестовая вещь2"));
    }

    @Test
    @SneakyThrows
    void findAll_returnsEmptyList() {
        List<Item> emptyItems = Collections.emptyList();
        List<ItemResponseDto> emptyResponseDtos = Collections.emptyList();

        when(itemService.findAll(eq(userId), eq(from), eq(size))).thenReturn(emptyItems);
        when(itemMapper.toResponse(emptyItems)).thenReturn(emptyResponseDtos);


        mockMvc.perform(get("/items")
                        .header(USER_HEADER, userId)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @SneakyThrows
    void findAll_nullUserIdReturnsBadRequest() {
        mockMvc.perform(get("/items")
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void create_returnsCreatedComment() {
        Long userId = 2L;
        Long itemId = 2L;
        String commentText = "комментарий ";

        User user = new User();
        user.setId(userId);
        user.setName("Тестовый пользователь");

        CommentCreateDto commentCreateDto = new CommentCreateDto();
        commentCreateDto.setText(commentText);

        Comment createdComment = new Comment();
        createdComment.setId(3L);
        createdComment.setText(commentText);
        createdComment.setAuthor(user);
        createdComment.setCreated(LocalDateTime.now());

        CommentResponseDto expectedResponse = new CommentResponseDto();
        expectedResponse.setId(createdComment.getId());
        expectedResponse.setText(commentText);
        expectedResponse.setAuthorName(user.getName());
        expectedResponse.setCreated(createdComment.getCreated());

        when(userService.findById(userId)).thenReturn(user);
        when(commentService.addComment(itemId, user, commentText)).thenReturn(createdComment);
        when(commentMapper.toResponse(createdComment)).thenReturn(expectedResponse);

        mockMvc.perform(post("/items/{itemId}/comment", itemId)
                        .header(USER_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentCreateDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(expectedResponse.getId()))
                .andExpect(jsonPath("$.text").value(expectedResponse.getText()))
                .andExpect(jsonPath("$.authorName").value(expectedResponse.getAuthorName()))
                .andExpect(jsonPath("$.created").exists());
    }


    @Test
    @SneakyThrows
    void create_userNotFound_ThrowsNotFoundException() {

        CommentCreateDto commentCreateDto = new CommentCreateDto();
        commentCreateDto.setText("Комментарий от несуществующего пользователя");

        when(userService.findById(userId)).thenThrow(new NotFoundException("Пользователь по данному ID не найден"));

        mockMvc.perform(post("/{itemId}/comment", itemId)
                        .header(USER_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentCreateDto)))
                .andExpect(status().isNotFound());
    }


    @Test
    @SneakyThrows
    void getItemWithComments_itemNotFoundException() {
        when(itemService.findById(itemId)).thenThrow(new NotFoundException("Вещь для проката по данному ID не найдена"));
        mockMvc.perform(post("/{itemId}/comment", itemId)
                        .header(USER_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

}


