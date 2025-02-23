package com.practice.shareitserver.item.itemDto;

import com.practice.shareitserver.item.Item;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ItemShortResponseDto {
    private Long id;
    private String name;
    private String description;
    private Long requestId;
    private boolean available;
    List<Item> itemList;
}
