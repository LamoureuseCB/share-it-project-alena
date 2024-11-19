package com.practice.shareitprojectalena.comment;

import com.practice.shareitprojectalena.item.entity.Item;
import com.practice.shareitprojectalena.user.entity.User;

import lombok.*;

import java.time.LocalDateTime;



@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Comment {

    private Long id;
    private String text;
    private Item item;
    private User author;
    private LocalDateTime created;

}
