package com.practice.shareitprojectalena.item.comment;

import com.practice.shareitprojectalena.booking.BookingRepository;
import com.practice.shareitprojectalena.error.exceptions.ConflictException;
import com.practice.shareitprojectalena.item.Item;
import com.practice.shareitprojectalena.item.ItemService;
import com.practice.shareitprojectalena.user.entity.User;
import com.practice.shareitprojectalena.utils.BookingStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final BookingRepository bookingRepository;
    private final ItemService itemService;

    public List<Comment> findByItemId(Long itemId){
        return commentRepository.findByItemId(itemId);
    }



    public Comment addComment(Long itemId, User author, String description) {
        boolean authorHasBooking = bookingRepository.findByItem_IdAndBooker_IdAndStatusAndEndBefore(itemId, author.getId(), BookingStatus.APPROVED, LocalDateTime.now());
        if (!authorHasBooking) {
            throw new ConflictException("Нельзя оставить комментарий если вы не бронировали вещь и не пользовались сервисом Share it");
        }
        Item item = itemService.findById(itemId);
        Comment comment = new Comment();
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setDescription(description);
        comment.setCreated(LocalDateTime.now());
        return commentRepository.save(comment);
    }

}
