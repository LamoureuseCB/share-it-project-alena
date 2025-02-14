package com.practice.shareitprojectalena.item.comment;

import com.practice.shareitprojectalena.item.comment.commentDto.CommentResponseDto;
import com.practice.shareitprojectalena.user.entity.User;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CommentMapper {
    public CommentResponseDto toResponse(Comment comment) {
        if(comment == null) {
            throw new IllegalArgumentException("Поле с комментарием должно быть заполнено");
        }
        User author = comment.getAuthor();
        String authorName = (author != null) ? author.getName() : "Автор отсутствует";

        return CommentResponseDto.builder()
                .id(comment.getId())
                .authorName(authorName)
                .text(comment.getText())
                .created(comment.getCreated())
                .build();
    }

    public List<CommentResponseDto> toResponse(List<Comment> comments) {
        return comments.stream()
                .map(this::toResponse)
                .toList();
    }
}
