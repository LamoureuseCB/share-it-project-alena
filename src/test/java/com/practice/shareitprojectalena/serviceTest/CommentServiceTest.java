package com.practice.shareitprojectalena.serviceTest;

import com.practice.shareitprojectalena.booking.BookingRepository;
import com.practice.shareitprojectalena.item.Item;
import com.practice.shareitprojectalena.item.ItemService;
import com.practice.shareitprojectalena.item.comment.Comment;
import com.practice.shareitprojectalena.item.comment.CommentRepository;
import com.practice.shareitprojectalena.item.comment.CommentService;
import com.practice.shareitprojectalena.user.entity.User;
import com.practice.shareitprojectalena.utils.BookingStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CommentServiceTest {
    @InjectMocks
    private CommentService commentService;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private ItemService itemService;
    private final Long itemId = 1L;

    @Test
    void findByItemIdSuccess() {
        Comment comment = new Comment();
        comment.setId(1);
        comment.setItem(new Item());
        comment.getItem().setId(itemId);
        List<Comment> expectedComments = new ArrayList<>();
        expectedComments.add(comment);

        when(commentRepository.findByItemId(itemId)).thenReturn(expectedComments);

        List<Comment> actualComments = commentService.findByItemId(itemId);

        assertEquals(expectedComments, actualComments);

    }

    @Test
    void addComment_WhenUserHasBooking() {
        User author = new User();
        author.setId(2L);
        String description = "Описание комментария";

        Item item = new Item();
        item.setId(itemId);

        when(bookingRepository.existsByItem_IdAndBooker_IdAndStatusAndEndBefore(
                eq(itemId), eq(author.getId()), eq(BookingStatus.APPROVED), any(LocalDateTime.class)))
                .thenReturn(true);
        when(itemService.findById(itemId)).thenReturn(item);

        Comment savedComment = new Comment();
        savedComment.setId(3);
        savedComment.setText(description);
        savedComment.setAuthor(author);
        savedComment.setItem(item);
        savedComment.setCreated(LocalDateTime.now());

        when(commentRepository.save(any(Comment.class))).thenReturn(savedComment);

        Comment actualComment = commentService.addComment(itemId, author, description);

        assertNotNull(actualComment);
        assertEquals(savedComment.getId(), actualComment.getId());
        assertEquals(description, actualComment.getText());
        assertEquals(author, actualComment.getAuthor());
        assertEquals(item, actualComment.getItem());
        assertNotNull(actualComment.getCreated());
}

    @Test
    void addComment_NoBookingThrowException() {
        User author = new User();
        author.setId(2L);
        String description = "Описание комментария";

        when(bookingRepository.existsByItem_IdAndBooker_IdAndStatusAndEndBefore(
                eq(itemId), eq(author.getId()), eq(BookingStatus.APPROVED), any(LocalDateTime.class)))
                .thenReturn(false);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> commentService.addComment(itemId, author, description));
        assertEquals("Нельзя оставить комментарий если вы не бронировали вещь и не пользовались сервисом Share it",
                exception.getMessage());
    }

    @Test
    void findByItemId_NoCommentsReturnsEmptyList() {

        when(commentRepository.findByItemId(itemId)).thenReturn(Collections.emptyList());

        List<Comment> comments = commentService.findByItemId(itemId);

        assertTrue(comments.isEmpty());
    }

}
