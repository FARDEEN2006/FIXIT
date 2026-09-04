package com.fixit.dto; import lombok.*; import java.time.LocalDateTime;
@Data @NoArgsConstructor @AllArgsConstructor @Builder public class AuthLoginResponse { private String token; private String userId; private String email; private boolean isAdmin; private String message; private LocalDateTime issuedAt; private LocalDateTime expiresAt; }
