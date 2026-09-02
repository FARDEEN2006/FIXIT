package com.fixit.service;

import com.fixit.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Auth Service
 * 
 * Handles:
 * - Token validation
 * - User authentication checks
 * - Admin verification
 */
@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    /**
     * Validate JWT token
     */
    public boolean validateToken(String token) {
        try {
            return jwtTokenProvider.validateToken(token);
        } catch (Exception e) {
            logger.debug("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Get user ID from token
     */
    public String getUserIdFromToken(String token) {
        try {
            return jwtTokenProvider.getUserIdFromJWT(token);
        } catch (Exception e) {
            logger.error("Failed to extract user ID from token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Get email from token
     */
    public String getEmailFromToken(String token) {
        try {
            return jwtTokenProvider.getEmailFromJWT(token);
        } catch (Exception e) {
            logger.error("Failed to extract email from token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Check if user is admin
     */
    public boolean isAdmin(String token) {
        try {
            return jwtTokenProvider.isAdmin(token);
        } catch (Exception e) {
            logger.debug("Admin check failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Generate token (usually called after Supabase Auth login)
     */
    public String generateToken(String userId, String email, boolean isAdmin) {
        try {
            logger.info("Generating token for user: {} (admin: {})", userId, isAdmin);
            return jwtTokenProvider.generateToken(userId, email, isAdmin);
        } catch (Exception e) {
            logger.error("Failed to generate token: {}", e.getMessage());
            return null;
        }
    }
}
