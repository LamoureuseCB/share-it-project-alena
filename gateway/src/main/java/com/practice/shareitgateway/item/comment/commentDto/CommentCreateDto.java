package com.practice.shareitgateway.item.comment.commentDto;

import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentCreateDto {
    @Size(max = 122)
    private String text;

}
