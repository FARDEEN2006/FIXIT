package com.fixit.dto; import lombok.*; import java.time.LocalDateTime;
@Data @NoArgsConstructor @AllArgsConstructor @Builder public class ApiResponse<T> { private boolean success; private String message; private T data; private LocalDateTime timestamp; public ApiResponse(boolean success,String message,T data){this.success=success;this.message=message;this.data=data;} }
