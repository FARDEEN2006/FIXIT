package com.fixit.dto; import jakarta.validation.constraints.*; import lombok.*;
@Data @NoArgsConstructor @AllArgsConstructor public class SecondHandListingStatusUpdate { @NotBlank(message="Status is required") private String status; }
