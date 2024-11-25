package com.practice.shareitprojectalena.item.itemDto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.practice.shareitprojectalena.booking.dto.BookingResponseDto;
import com.practice.shareitprojectalena.item.comment.Comment;
import com.practice.shareitprojectalena.item.comment.commentDto.CommentResponseDto;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class ItemResponseDto {
    private Long id;
    private String name;
    private String description;
    @JsonProperty("available")
    private boolean available;
    private List<CommentResponseDto> comments;
    private BookingResponseDto lastBooking;
    private BookingResponseDto nextBooking;



}
