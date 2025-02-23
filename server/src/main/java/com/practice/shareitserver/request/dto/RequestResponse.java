package com.practice.shareitserver.request.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class RequestResponse {
    private Long id;
    private String description;
    private LocalDateTime created;
}
