package com.practice.shareitprojectalena.item.dto;

import com.practice.shareitprojectalena.comment.Comment;
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
    private boolean isAvailable;
    private Long ownerId;
    private List<Comment> comments;
}
