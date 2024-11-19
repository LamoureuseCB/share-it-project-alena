package com.practice.shareitprojectalena.item.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ItemResponseDto {
    private Long id;
    private String name;
    private String description;
    @JsonProperty("available")
    private boolean available;
//    private Long ownerId;
//    private List<Comment> comments;
}
