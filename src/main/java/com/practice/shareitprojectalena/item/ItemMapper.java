package com.practice.shareitprojectalena.item;


import com.practice.shareitprojectalena.booking.Booking;
import com.practice.shareitprojectalena.booking.BookingMapper;
import com.practice.shareitprojectalena.booking.BookingRepository;
import com.practice.shareitprojectalena.booking.dto.BookingResponseDto;
import com.practice.shareitprojectalena.item.comment.Comment;
import com.practice.shareitprojectalena.item.comment.CommentMapper;
import com.practice.shareitprojectalena.item.comment.CommentService;
import com.practice.shareitprojectalena.item.itemDto.ItemCreateDto;
import com.practice.shareitprojectalena.item.itemDto.ItemResponseDto;
import com.practice.shareitprojectalena.item.itemDto.ItemUpdateDto;

import com.practice.shareitprojectalena.user.UserMapper;
import com.practice.shareitprojectalena.utils.BookingStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;


@Component
@RequiredArgsConstructor
public class ItemMapper {
    private final CommentMapper commentMapper;
    private final BookingRepository bookingRepository;
    private final UserMapper userMapper;

    public Item fromCreate(ItemCreateDto itemCreateDto) {
        return Item.builder()
                .name(itemCreateDto.getName())
                .description(itemCreateDto.getDescription())
                .isAvailable(itemCreateDto.getAvailable())
                .build();
    }

    public Item fromUpdate(ItemUpdateDto itemUpdateDto) {
        return Item.builder()
                .name(itemUpdateDto.getName())
                .description(itemUpdateDto.getDescription())
                .isAvailable(itemUpdateDto.getIsAvailable())
                .build();
    }

    public void merge(Item existingItem, Item updatedItem) {
        if (updatedItem.getName() != null && !updatedItem.getName().isBlank()) {
            existingItem.setName(updatedItem.getName());
        }
        if (updatedItem.getDescription() != null && !updatedItem.getDescription().isBlank()) {
            existingItem.setDescription(updatedItem.getDescription());
        }
        if (updatedItem.getIsAvailable() != null) {
            existingItem.setIsAvailable(updatedItem.getIsAvailable());
        }
    }


    public ItemResponseDto toResponse(Item item) {
        return ItemResponseDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getIsAvailable())
//                .comments(commentMapper.toResponse(item.getComments()))
                .build();
    }

    public List<ItemResponseDto> toResponse(List<Item> items) {
        return items.stream().map(this::toResponse).toList();
    }

    public ItemResponseDto toResponseWithComments(Item item, List<Comment> comments) {
        Booking booking = bookingRepository.findByItem_IdAndStatusIsAndStartIsBeforeOrderByStartDesc(item.getId(), BookingStatus.APPROVED, LocalDateTime.now().minusSeconds(4))
                .stream()
                .findFirst()
                .orElse(null);


        return ItemResponseDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getIsAvailable())
                .comments(commentMapper.toResponse(comments))
                .lastBooking(booking == null ? null : this.toResponse(booking))
                .build();

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