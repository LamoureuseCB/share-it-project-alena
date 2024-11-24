package com.practice.shareitprojectalena.item.itemDto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ItemUpdateDto {
    private String name;
    private String description;
   @JsonProperty("available")
    private Boolean isAvailable;
}
