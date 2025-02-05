package com.practice.shareitprojectalena.item.comment.commentDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommentResponseDto {
   private int id;
   private String text;
   private String authorName;
   private LocalDateTime created;
}
