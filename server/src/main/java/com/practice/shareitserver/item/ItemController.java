package com.practice.shareitserver.item;


import com.practice.shareitserver.error.exceptions.InvalidPageException;
import com.practice.shareitserver.error.exceptions.InvalidSizeException;
import com.practice.shareitserver.error.exceptions.NotFoundException;
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
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static com.practice.shareitserver.utils.RequestConstants.USER_HEADER;


@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemController {
    private final ItemService itemService;
    private final ItemMapper itemMapper;
    private final UserService userService;
    private final CommentService commentService;
    private final CommentMapper commentMapper;

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public ItemResponseDto create(@RequestHeader(USER_HEADER) Long userId,@RequestBody ItemCreateDto itemCreateDto) {
        Item item = itemMapper.fromCreate(itemCreateDto);
        Item createdItem = itemService.create(item, userId);
        return itemMapper.toResponse(createdItem);
    }

    @PatchMapping("/{itemId}")
    public ItemResponseDto update(@RequestHeader(USER_HEADER) Long userId,
                                  @PathVariable Long itemId,
                                  @RequestBody ItemUpdateDto itemUpdateDto) {

        Item item = itemMapper.fromUpdate(itemUpdateDto);
        Item updatedItem = itemService.update(item, itemId, userId);
        return itemMapper.toResponse(updatedItem);
    }


    @GetMapping
    public List<ItemResponseDto> findAll(@RequestHeader(USER_HEADER) Long userId,
                                         @RequestParam(defaultValue = "0") int from,
                                         @RequestParam(defaultValue = "10") int size) throws InvalidSizeException, InvalidPageException {
        List<Item> items = itemService.findAll(userId, from, size);
        return itemMapper.toResponse(items);
    }

    @GetMapping("/search")
    public List<ItemResponseDto> searchItems(@RequestParam String text, @RequestParam int from, @RequestParam int size) throws InvalidSizeException, InvalidPageException {
        List<Item> items = itemService.searchItems(text, from, size);
        return items.stream()
                .map(itemMapper::toResponse)
                .collect(Collectors.toList());
    }


    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        itemService.delete(id);
    }

    @PostMapping("/{itemId}/comment")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponseDto create(@RequestHeader(USER_HEADER) Long userId, @RequestBody CommentCreateDto commentCreateDto, @PathVariable Long itemId) {
        User user = userService.findById(userId);
        if(user == null){
            throw new NotFoundException("Пользователь не найден");
        }
            return commentMapper.toResponse(commentService.addComment(itemId, user, commentCreateDto.getText()));

    }


    @GetMapping("/{itemId}")
    public ItemResponseDto getItemWithComments(@PathVariable Long itemId) {
        Item item = itemService.findById(itemId);
        if (item == null) {
            throw new NotFoundException("Вещь не найдена");
        }
        List<Comment> comments = commentService.findByItemId(itemId);
        return itemMapper.toResponseWithComments(item,comments);
    }



}

