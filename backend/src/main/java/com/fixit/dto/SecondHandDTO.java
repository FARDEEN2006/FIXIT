package com.fixit.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Second-Hand Listing DTOs
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SecondHandListingRequest {
    @NotBlank(message = "Seller name is required")
    @Size(min = 2, max = 255)
    private String sellerName;

    @NotBlank(message = "Seller phone is required")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid phone format")
    private String sellerPhone;

    @NotBlank(message = "Seller email is required")
    @Email(message = "Invalid email format")
    private String sellerEmail;

    @NotBlank(message = "Product name is required")
    @Size(min = 3, max = 255)
    private String productName;

    @NotBlank(message = "Condition is required")
    private String condition; // NEW, GOOD, FAIR, POOR

    @NotBlank(message = "Detailed description is required")
    @Size(min = 20, max = 2000)
    private String detailedDescription;

    @NotNull(message = "Expected price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private BigDecimal expectedPrice;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SecondHandListingResponse {
    private String id;
    private String sellerName;
    private String sellerPhone;
    private String sellerEmail;
    private String productName;
    private String condition;
    private String detailedDescription;
    private BigDecimal expectedPrice;
    private String listingStatus;
    private boolean emailVerified;
    private LocalDateTime emailVerifiedAt;
    private List<SecondHandImageResponse> images;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SecondHandImageResponse {
    private String id;
    private String storagePath;
    private String imageUrl;
    private int imageOrder;
    private boolean isThumbnail;
    private LocalDateTime createdAt;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SecondHandListingListResponse {
    private List<SecondHandListingResponse> listings;
    private int page;
    private int pageSize;
    private long totalCount;
}

// Email verification requests
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailVerificationRequest {
    @NotBlank(message = "Listing ID is required")
    private String listingId;

    @NotBlank(message = "Verification token is required")
    private String token;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailVerificationResponse {
    private boolean success;
    private String message;
    private String listingId;
    private String nextStep; // "upload_images" or similar
}

// Status update
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SecondHandListingStatusUpdate {
    @NotBlank(message = "Status is required")
    private String status; // NEW, REVIEWING, CONTACTED, ACCEPTED, REJECTED, COMPLETED
}
