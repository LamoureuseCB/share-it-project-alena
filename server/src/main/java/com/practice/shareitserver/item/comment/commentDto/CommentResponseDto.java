package com.practice.shareitserver.item.comment.commentDto;

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
   private Long id;
   private String text;
   private String authorName;
   private LocalDateTime created;
}
