package com.practice.shareitprojectalena.request.dto;

import com.practice.shareitprojectalena.item.Item;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemRequestFullDto {
    private Long id;
    private String description;
    private Long requesterId;
    private LocalDateTime created;
    private List<Item> items;
}
