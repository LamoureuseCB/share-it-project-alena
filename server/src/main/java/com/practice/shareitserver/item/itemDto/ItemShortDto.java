package com.practice.shareitserver.item.itemDto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ItemShortDto {
    private Long id;
    private String name;
    private String description;
}
