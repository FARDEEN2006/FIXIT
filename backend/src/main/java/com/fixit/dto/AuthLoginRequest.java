package com.fixit.dto; import jakarta.validation.constraints.*; import lombok.*;
@Data @NoArgsConstructor @AllArgsConstructor @Builder public class AuthLoginRequest { @NotBlank(message="Email is required") @Email(message="Invalid email format") private String email; @NotBlank(message="Password is required") private String password; }
