package com.fixit.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductRequest {

    @NotBlank(message = "Product name is required")
    @Size(
            min = 3,
            max = 255,
            message = "Product name must be between 3 and 255 characters"
    )
    private String name;

    @NotNull(message = "Price is required")
    @DecimalMin(
            value = "0.01",
            message = "Price must be greater than 0"
    )
    private BigDecimal price;

    @NotBlank(message = "Product description is required")
    @Size(
            min = 10,
            max = 2000,
            message = "Product description must be between 10 and 2000 characters"
    )
    private String description;

    @Builder.Default
    @JsonProperty("isActive")
    private boolean isActive = true;
}