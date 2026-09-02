package com.fixit.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Product Enquiry DTOs
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductEnquiryRequest {
    @NotBlank(message = "Product ID is required")
    private String productId;

    @NotBlank(message = "Customer name is required")
    @Size(min = 2, max = 255, message = "Name must be between 2 and 255 characters")
    private String customerName;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid phone number format")
    private String customerPhone;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String customerEmail;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductEnquiryResponse {
    private String id;
    private String productId;
    private String productName;
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private String enquiryStatus;
    private String whatsappMessage;
    private LocalDateTime createdAt;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductEnquiryListResponse {
    private java.util.List<ProductEnquiryResponse> enquiries;
    private int page;
    private int pageSize;
    private long totalCount;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductEnquiryStatusUpdate {
    @NotBlank(message = "Status is required")
    private String status;
}
