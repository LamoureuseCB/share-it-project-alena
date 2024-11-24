package com.practice.shareitprojectalena.item.comment.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class CommentResponseDto {
   private int id;
   private String description;
   private String authorName;
   private LocalDateTime created;
   private List<CommentResponseDto> comments;

}
