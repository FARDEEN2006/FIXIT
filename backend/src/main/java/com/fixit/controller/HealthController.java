package com.fixit.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Health Check Controller
 * 
 * Provides health check endpoint for Render deployment monitoring
 */
@RestController
@RequestMapping("/api/health")
public class HealthController {

    private static final Logger logger = LoggerFactory.getLogger(HealthController.class);

    /**
     * Health check endpoint
     * GET /api/health
     * 
     * Response: { "status": "UP", "timestamp": "..." }
     */
    @GetMapping
    public Map<String, Object> health() {
        logger.info("Health check requested");
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", java.time.LocalDateTime.now());
        response.put("service", "FIXIT-Backend");
        response.put("version", "1.0.0");
        
        return response;
    }
}
