package com.practice.shareitprojectalena.controllerTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.practice.shareitprojectalena.error.exceptions.NotFoundException;
import com.practice.shareitprojectalena.item.*;
import com.practice.shareitprojectalena.item.comment.Comment;
import com.practice.shareitprojectalena.item.comment.CommentMapper;
import com.practice.shareitprojectalena.item.comment.CommentService;
import com.practice.shareitprojectalena.item.comment.commentDto.CommentCreateDto;
import com.practice.shareitprojectalena.item.comment.commentDto.CommentResponseDto;
import com.practice.shareitprojectalena.item.itemDto.ItemCreateDto;
import com.practice.shareitprojectalena.item.itemDto.ItemResponseDto;
import com.practice.shareitprojectalena.item.itemDto.ItemUpdateDto;
import com.practice.shareitprojectalena.user.UserRepository;
import com.practice.shareitprojectalena.user.UserService;
import com.practice.shareitprojectalena.user.entity.User;
import lombok.SneakyThrows;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.practice.shareitprojectalena.utils.RequestConstants.USER_HEADER;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
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
    private ItemMapper itemMapper;
    @MockBean
    private CommentMapper commentMapper;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private ItemRepository itemRepository;

    private final Long userId = 1L;

    private User booker;
    private final Long bookerId = 2L;
    private Item item;
    private final Long itemId = 1L;

    private final int from = 0;
    private final int size = 10;

    @BeforeEach
    void setUp() {

        User owner = new User();
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

        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        when(itemMapper.fromCreate(any(ItemCreateDto.class))).thenReturn(item);
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


    @Test
    @SneakyThrows
    public void create_ItemSuccess() {
        ItemCreateDto itemCreateDto = createItemDto();
        Item mockingItem = createMockItem(itemCreateDto);
        ItemResponseDto itemResponseDto = createItemResponseDto(mockingItem);

        when(itemMapper.fromCreate(itemCreateDto)).thenReturn(mockingItem);
        when(itemService.create(any(Item.class), eq(userId))).thenReturn(mockingItem);
        when(itemMapper.toResponse(any(Item.class))).thenReturn(itemResponseDto);

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
    public void testUpdate_ItemSuccess() {
        Long itemId = 1L;
        Long userId = 1L;

        ItemUpdateDto itemUpdateDto = new ItemUpdateDto();
        itemUpdateDto.setName("Обновленная вещь");
        itemUpdateDto.setDescription("Обновленное описание");
        itemUpdateDto.setIsAvailable(false);

        Item updatedItem = new Item();
        updatedItem.setId(itemId);
        updatedItem.setName(itemUpdateDto.getName());
        updatedItem.setDescription(itemUpdateDto.getDescription());
        updatedItem.setIsAvailable(itemUpdateDto.getIsAvailable());

        ItemResponseDto itemResponseDto = new ItemResponseDto();
        itemResponseDto.setName(updatedItem.getName());
        itemResponseDto.setId(updatedItem.getId());
        itemResponseDto.setAvailable(updatedItem.getIsAvailable());
        itemResponseDto.setDescription(itemUpdateDto.getDescription());


        when(itemService.update(any(Item.class), eq(itemId), eq(userId))).thenReturn(updatedItem);
        when(itemMapper.toResponse(updatedItem)).thenReturn(itemResponseDto);

        mockMvc.perform(patch("/items/{itemId}", itemId)
                        .header(USER_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemUpdateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", Matchers.is(itemId.intValue())))
                .andExpect(jsonPath("$.name", Matchers.is("Обновленная вещь")))
                .andExpect(jsonPath("$.description", Matchers.is("Обновленное описание")))
                .andExpect(jsonPath("$.available", Matchers.is(false)));
    }


    @Test
    @SneakyThrows
    public void testUpdateUnsuccessfully() {
        Long nonExistentItemId = 99999999L;

        ItemUpdateDto itemUpdateDto = new ItemUpdateDto();
        itemUpdateDto.setName("Вещь с несуществующим ID");
        itemUpdateDto.setDescription("Описание");
        itemUpdateDto.setIsAvailable(false);

        when(itemService.update(any(Item.class), eq(nonExistentItemId), eq(userId)))
                .thenThrow(new NotFoundException("Объект не найден"));

        mockMvc.perform(patch("/items/{itemId}", nonExistentItemId)
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
                .andExpect(jsonPath("$", Matchers.hasSize(size)));
//                .andExpect(jsonPath("$[0].name", Matchers.is("Тестовая вещь 1")))
//                .andExpect(jsonPath("$[1].name", Matchers.is("Тестовая вещь 2")));
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

        String text = "Комментарий";
        Comment comment = new Comment();
        comment.setId(1);
        comment.setText(text);
        comment.setAuthor(booker);
        comment.setCreated(LocalDateTime.now());

        CommentCreateDto commentCreateDto = new CommentCreateDto();
        commentCreateDto.setText(comment.getText());

        CommentResponseDto commentResponseDto = CommentResponseDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .authorName(booker.getName())
                .created(comment.getCreated())
                .build();


        when(commentService.addComment(eq(itemId), eq(booker), eq(text))).thenReturn(comment);
        when(commentMapper.toResponse(comment)).thenReturn(commentResponseDto);


        mockMvc.perform(post("/{itemId}/comment", itemId)
                        .header(USER_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentCreateDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(comment.getId()))
                .andExpect(jsonPath("$.text").value(text))
                .andExpect(jsonPath("$.authorName").value(booker.getName()));
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
    void getItemWithCommentsSuccess() {

        Long itemId = 1L;
        Item item = new Item();
        item.setId(itemId);
        item.setName("Тестовая вещь");


        Comment comment1 = new Comment();
        comment1.setId(1);
        comment1.setText("Комментарий 1");

        Comment comment2 = new Comment();
        comment2.setId(2);
        comment2.setText("Комментарий 2");

        List<Comment> comments = List.of(comment1, comment2);


        when(itemService.findById(itemId)).thenReturn(item);
        when(commentService.findByItemId(itemId)).thenReturn(comments);


        CommentResponseDto commentDto1 = CommentResponseDto.builder()
                .id(1)
                .text("Комментарий 1")
                .build();

        CommentResponseDto commentDto2 = CommentResponseDto.builder()
                .id(2)
                .text("Комментарий 2")
                .build();

        ItemResponseDto expectedDto = ItemResponseDto.builder()
                .id(itemId)
                .name("Тестовая вещь")
                .comments(List.of(commentDto1, commentDto2))
                .build();


        when(itemMapper.toResponseWithComments(item, comments)).thenReturn(expectedDto);



        mockMvc.perform(get("/items/{itemId}", itemId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemId))
                .andExpect(jsonPath("$.name").value("Тестовая вещь"))
                .andExpect(jsonPath("$.comments.length()").value(2))
                .andExpect(jsonPath("$.comments[0].id").value(1L))
                .andExpect(jsonPath("$.comments[0].text").value("Комментарий 1"))
                .andExpect(jsonPath("$.comments[1].id").value(2L))
                .andExpect(jsonPath("$.comments[1].text").value("Комментарий 2"));
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

    @Test
    @SneakyThrows
    void getItemWithCommentsReturnsItemWithEmptyComments() {
        when(itemService.findById(itemId)).thenReturn(item);
        when(commentService.findByItemId(itemId)).thenReturn(Collections.emptyList());

        ItemResponseDto responseDto = ItemResponseDto.builder()
                .id(itemId)
                .name("Тестовая вещь")
                .comments(Collections.emptyList())
                .build();


        when(itemMapper.toResponseWithComments(item, Collections.emptyList())).thenReturn(responseDto);


        mockMvc.perform(get("/items/{itemId}", itemId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemId))
                .andExpect(jsonPath("$.name").value("Тестовая вещь"))
                .andExpect(jsonPath("$.comments").isEmpty());
    }

}
