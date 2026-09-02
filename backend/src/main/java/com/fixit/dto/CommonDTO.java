package com.fixit.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Authentication DTOs
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthLoginRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthLoginResponse {
    private String token;
    private String userId;
    private String email;
    private boolean isAdmin;
    private String message;
    private LocalDateTime issuedAt;
    private LocalDateTime expiresAt;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthErrorResponse {
    private boolean success;
    private String message;
    private String error;
}

/**
 * Store Information DTOs
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreInformationResponse {
    private String id;
    private String businessName;
    private String phoneNumber;
    private String whatsappNumber;
    private String email;
    private String address;
    private String city;
    private String state;
    private String pincode;
    private String workingHoursMonday;
    private String workingHoursSaturday;
    private String workingHoursSunday;
    private String aboutContent;
    private Double mapLat;
    private Double mapLon;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreInformationRequest {
    @NotBlank(message = "Business name is required")
    private String businessName;

    @NotBlank(message = "Phone number is required")
    private String phoneNumber;

    @NotBlank(message = "WhatsApp number is required")
    private String whatsappNumber;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "State is required")
    private String state;

    @NotBlank(message = "Pincode is required")
    private String pincode;

    private String workingHoursMonday;
    private String workingHoursSaturday;
    private String workingHoursSunday;
    private String aboutContent;
    private Double mapLat;
    private Double mapLon;
}

/**
 * Service DTOs
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceResponse {
    private String id;
    private String serviceName;
    private String description;
    private String iconPath;
    private String iconUrl;
    private boolean isActive;
    private int displayOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceRequest {
    @NotBlank(message = "Service name is required")
    @Size(min = 3, max = 255)
    private String serviceName;

    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 1000)
    private String description;

    @Builder.Default
    private boolean isActive = true;

    @Builder.Default
    private int displayOrder = 0;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceListResponse {
    private java.util.List<ServiceResponse> services;
}

/**
 * Generic Response DTOs
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private LocalDateTime timestamp;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiErrorResponse {
    private boolean success;
    private String message;
    private String error;
    private int statusCode;
    private LocalDateTime timestamp;
}
