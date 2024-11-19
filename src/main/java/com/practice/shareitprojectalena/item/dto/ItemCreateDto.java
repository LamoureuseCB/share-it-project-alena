package com.practice.shareitprojectalena.item.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ItemCreateDto {
    private String name;
    private String description;
    @NotNull
    private Boolean available;
}