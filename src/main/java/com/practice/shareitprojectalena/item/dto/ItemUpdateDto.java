package com.practice.shareitprojectalena.item.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ItemUpdateDto {
    private String name;
    private String description;
    private Boolean isAvailable;
}
