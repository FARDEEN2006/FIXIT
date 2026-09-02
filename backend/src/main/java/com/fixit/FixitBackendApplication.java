package com.fixit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * FIXIT Mobile Sales & Services - Backend Application
 * 
 * Spring Boot REST API for:
 * - Product management
 * - Product enquiries
 * - Second-hand listings
 * - Image upload & compression
 * - Admin authentication
 * - Email verification
 * 
 * Database: Supabase PostgreSQL
 * Storage: Supabase Storage
 * Authentication: Supabase Auth + JWT
 */
@SpringBootApplication
@EnableAsync
public class FixitBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(FixitBackendApplication.class, args);
    }
}
