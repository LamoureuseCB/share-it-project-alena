package com.practice.shareitserver.item.itemDto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.practice.shareitserver.booking.dto.BookingResponseDto;
import com.practice.shareitserver.item.comment.commentDto.CommentResponseDto;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
