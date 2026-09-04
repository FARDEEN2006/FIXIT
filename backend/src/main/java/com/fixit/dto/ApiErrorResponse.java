package com.fixit.dto;

import java.time.LocalDateTime;
import java.util.Map;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ApiErrorResponse {
    private boolean success;
    private String message;
    private Map<String, String> error;
    private int statusCode;
    private LocalDateTime timestamp;
}
