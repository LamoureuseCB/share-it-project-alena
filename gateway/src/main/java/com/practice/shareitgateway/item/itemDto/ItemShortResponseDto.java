package com.practice.shareitgateway.item.itemDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
}
