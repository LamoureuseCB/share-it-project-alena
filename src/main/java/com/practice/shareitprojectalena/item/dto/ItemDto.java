package com.practice.shareitprojectalena.item.dto;

import com.practice.shareitprojectalena.comment.Comment;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ItemDto {
    private Long id;
    private String name;
    private String description;
    private boolean isAvailable;
    private Long ownerId;
    private List<Comment> comments;
}
