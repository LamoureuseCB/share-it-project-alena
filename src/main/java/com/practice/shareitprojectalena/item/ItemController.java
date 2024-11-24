package com.practice.shareitprojectalena.item;


import com.practice.shareitprojectalena.item.comment.Comment;
import com.practice.shareitprojectalena.item.comment.CommentService;
import com.practice.shareitprojectalena.item.itemDto.ItemCreateDto;

import com.practice.shareitprojectalena.item.itemDto.ItemResponseDto;
import com.practice.shareitprojectalena.item.itemDto.ItemUpdateDto;


import com.practice.shareitprojectalena.user.entity.User;
import com.practice.shareitprojectalena.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;


import static com.practice.shareitprojectalena.utils.RequestConstants.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemController {
    private final ItemService itemService;
    private final ItemMapper itemMapper;
    private UserService userService;
    private final CommentService commentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemResponseDto create(@RequestHeader(USER_HEADER) Long userId, @RequestBody @Valid ItemCreateDto itemCreateDto) {
        Item item = itemMapper.fromCreate(itemCreateDto);
        Item createdItem = itemService.create(item, userId);
        return itemMapper.toResponse(createdItem);
    }

    @PatchMapping("/{itemId}")
    public ItemResponseDto update(@RequestHeader(USER_HEADER) Long userId,
                                  @PathVariable Long itemId,
                                  @RequestBody @Valid ItemUpdateDto itemUpdateDto) {
        Item item = itemMapper.fromUpdate(itemUpdateDto);
        Item updatedItem = itemService.update(item, itemId, userId);
        return itemMapper.toResponse(updatedItem);
    }

    @GetMapping("/{id}")
    public ItemResponseDto findById(@PathVariable Long id) {
        Item item = itemService.findById(id);
        return itemMapper.toResponse(item);
    }

    @GetMapping
    public List<ItemResponseDto> findAll(@RequestHeader(USER_HEADER) Long userId) {
        List<Item> items = itemService.findAll(userId);
        return itemMapper.toResponse(items);
    }

    @GetMapping("/search")
    public List<ItemResponseDto> searchItems(@RequestParam(value = "text", required = false) String text) {
        List<Item> items = itemService.searchItems(text);
        return items.stream()
                .map(itemMapper::toResponse)
                .toList();
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        itemService.delete(id);
    }

    @PostMapping("/{itemId}/comment")
    @ResponseStatus(HttpStatus.CREATED)
    public Comment create(@RequestHeader(USER_HEADER) Long userId, @RequestBody @Valid String description, @PathVariable Long itemId) {
        User user = userService.findById(userId);
        return commentService.addComment(itemId, user, description);

    }

    @GetMapping("/{itemId}")
    public ItemResponseDto getItemWithComments(@PathVariable Long itemId) {
        Item item = itemService.findById(itemId);
        List<Comment> comments = commentService.findByItemId(itemId);
        return itemMapper.toResponseWithComments(item, comments);
    }

    @GetMapping
    public List<ItemResponseDto> getAllItemComments(Long userId) {
        List<Item> items = itemService.findAll(userId);
        return items.stream()
                .map(item -> {
                    List<Comment> comments = commentService.findByItemId(item.getId());
                    return itemMapper.toResponseWithComments(item, comments);
                }).toList();

    }
}


