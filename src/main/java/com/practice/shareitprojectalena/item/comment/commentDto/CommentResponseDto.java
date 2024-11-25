package com.practice.shareitprojectalena.item.comment.commentDto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class CommentResponseDto {
   private int id;
   private String text;
   private String authorName;
   private LocalDateTime created;


}
