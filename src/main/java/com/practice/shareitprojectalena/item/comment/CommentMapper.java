package com.practice.shareitprojectalena.item.comment;

import com.practice.shareitprojectalena.item.comment.commentDto.CommentResponseDto;
import org.springframework.stereotype.Component;

import java.util.List;
@Component
public class CommentMapper {
    public CommentResponseDto toResponse(Comment comment) {
        return CommentResponseDto.builder()
                .id(comment.getId())
                .authorName(comment.getAuthor().getName())
                .description(comment.getDescription())
                .created(comment.getCreated())
                .build();
    }

    public List<CommentResponseDto> toResponse(List<Comment> comments) {
        return comments.stream()
                .map(this::toResponse)
                .toList();
    }
}
