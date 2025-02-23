package com.practice.shareitserver.item;



import com.practice.shareitserver.booking.Booking;
import com.practice.shareitserver.booking.dto.BookingResponseDto;
import com.practice.shareitserver.item.comment.Comment;
import com.practice.shareitserver.item.comment.CommentMapper;
import com.practice.shareitserver.item.itemDto.ItemCreateDto;
import com.practice.shareitserver.item.itemDto.ItemResponseDto;
import com.practice.common.dto.ItemShortDto;
import com.practice.shareitserver.item.itemDto.ItemUpdateDto;
import com.practice.shareitserver.request.entity.ItemRequest;
import com.practice.shareitserver.user.UserMapper;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


@Component
@AllArgsConstructor
@NoArgsConstructor
public class ItemMapper {

    private CommentMapper commentMapper;
    private UserMapper userMapper;



    public Item fromCreate(ItemCreateDto itemCreateDto) {
        if (itemCreateDto == null) {
            throw new IllegalArgumentException("Поля для создания вещи видимо оказались пустыми, нужно заполнить");
        }
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setId(itemCreateDto.getRequestId());
        return Item.builder()
                .name(itemCreateDto.getName())
                .description(itemCreateDto.getDescription())
                .isAvailable(itemCreateDto.getAvailable())
                .request(itemRequest.getId() != null ? itemRequest : null)
                .build();
    }


    public Item fromUpdate(ItemUpdateDto itemUpdateDto) {
        if (itemUpdateDto == null) {
            throw new IllegalArgumentException("Поля для обновления вещи видимо оказались пустыми, нужно заполнить");
        }
        return Item.builder()
                .name(itemUpdateDto.getName())
                .description(itemUpdateDto.getDescription())
                .isAvailable(itemUpdateDto.getIsAvailable())
                .build();
    }

    public void merge(Item existingItem, Item updatedItem) {
        boolean isUpdated = false;

        if (updatedItem.getName() != null && !updatedItem.getName().isBlank()) {
            existingItem.setName(updatedItem.getName());
            isUpdated = true;
        }

        if (updatedItem.getDescription() != null && !updatedItem.getDescription().isBlank()) {
            existingItem.setDescription(updatedItem.getDescription());
            isUpdated = true;
        }

        existingItem.setIsAvailable(updatedItem.getIsAvailable());
        isUpdated = true;

        System.out.println("Объект был обновлён: " + existingItem.getName());
    }



    public ItemResponseDto toResponse(Item item) {
       if (item == null) {
            System.out.println("Предмет не указан!");
            return null;
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

    public List<ItemResponseDto> toResponse(List<Item> items) {
        return items.stream().map(this::toResponse).toList();
    }

    public ItemResponseDto toResponseWithComments(Item item, List<Comment> comments) {
        return ItemResponseDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getIsAvailable())
                .comments(commentMapper.toResponse(comments))
                .lastBooking(item.getLastBooking() != null ? this.toResponse(item.getLastBooking()) : null)
                .build();
    }
    public ItemShortDto toItemShortDto(Item item) {
        if (item == null) {
            return null;
        }

        return ItemShortDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .build();
    }
    public List<ItemShortDto> toItemShortDto(List<Item> items) {
        return items.stream().map(this::toItemShortDto).toList();
    }


    public BookingResponseDto toResponse(Booking booking) {
        return BookingResponseDto.builder()
                .id(booking.getId())
                .itemId(booking.getItem().getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .status(booking.getStatus())
                .booker(userMapper.toResponse(booking.getBooker()))
                .item(this.toResponse(booking.getItem()))
                .build();
    }
}