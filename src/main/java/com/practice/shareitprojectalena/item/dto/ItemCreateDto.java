package com.practice.shareitprojectalena.item.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ItemCreateDto {
    private String name;
    private String description;
    private boolean isAvailable;
    private Long ownerId;
}