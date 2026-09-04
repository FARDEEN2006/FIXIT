package com.fixit.dto; import jakarta.validation.constraints.*; import lombok.*;
@Data @NoArgsConstructor @AllArgsConstructor @Builder public class ProductEnquiryStatusUpdate { @NotBlank(message="Status is required") private String status; }
