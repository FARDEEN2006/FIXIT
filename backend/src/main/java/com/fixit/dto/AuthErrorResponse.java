package com.fixit.dto; import lombok.*;
@Data @NoArgsConstructor @AllArgsConstructor @Builder public class AuthErrorResponse { private boolean success; private String message; private String error; }
