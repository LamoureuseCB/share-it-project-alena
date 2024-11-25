package com.practice.shareitprojectalena.item.comment;

import com.practice.shareitprojectalena.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment,Long> {
    List<Comment> findByItemId(Long itemId);
}
