package com.practice.shareitprojectalena.item.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
