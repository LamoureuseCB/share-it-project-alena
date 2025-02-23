package com.practice.shareitgateway.item.itemDto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ItemUpdateDto {
    private String name;
    private String description;
   @JsonProperty("available")
    private Boolean isAvailable;
}
