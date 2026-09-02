package com.fixit.controller;

import com.fixit.config.SupabaseConfig;
import com.fixit.dto.ApiResponse;
import com.fixit.dto.AuthLoginRequest;
import com.fixit.dto.AuthLoginResponse;
import com.fixit.service.AuthService;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/** Exchanges Supabase credentials server-side and issues a FIXIT admin JWT. */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final SupabaseConfig supabaseConfig;
    private final RestTemplate restTemplate;
    private final AuthService authService;
    public AuthController(SupabaseConfig supabaseConfig, RestTemplate restTemplate, AuthService authService) {
        this.supabaseConfig = supabaseConfig; this.restTemplate = restTemplate; this.authService = authService;
    }
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthLoginResponse>> login(@Valid @RequestBody AuthLoginRequest request) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("apikey", supabaseConfig.getAnonKey()); headers.set("Content-Type", "application/json");
            ResponseEntity<Map> response = restTemplate.postForEntity(supabaseConfig.getSupabaseUrl() + "/auth/v1/token?grant_type=password", new org.springframework.http.HttpEntity<>(Map.of("email", request.getEmail(), "password", request.getPassword()), headers), Map.class);
            Map body = response.getBody(); Map user = body == null ? null : (Map) body.get("user");
            Map metadata = user == null ? null : (Map) user.get("user_metadata");
            boolean admin = metadata != null && Boolean.TRUE.equals(metadata.get("is_admin"));
            if (user == null || !admin) return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse<>(false, "Admin access is required", null));
            String userId = String.valueOf(user.get("id")); String email = String.valueOf(user.get("email"));
            String token = authService.generateToken(userId, email, true);
            AuthLoginResponse data = AuthLoginResponse.builder().token(token).userId(userId).email(email).isAdmin(true).message("Signed in").issuedAt(LocalDateTime.now()).build();
            return ResponseEntity.ok(new ApiResponse<>(true, "Signed in", data));
        } catch (Exception exception) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse<>(false, "Invalid credentials", null));
        }
    }
}
