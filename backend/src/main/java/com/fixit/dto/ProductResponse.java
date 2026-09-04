package com.fixit.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponse {

    private String id;

    private String name;

    private String description;

    private String imagePath;

    private String imageUrl;

    private BigDecimal price;

    @JsonProperty("isActive")
    private boolean isActive;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}