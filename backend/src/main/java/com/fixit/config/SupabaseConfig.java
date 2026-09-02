package com.fixit.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * Supabase Configuration
 * 
 * Contains all Supabase connection details
 * Injected from environment variables
 */
@Component
@Configuration
public class SupabaseConfig {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.anon-key}")
    private String anonKey;

    @Value("${supabase.service-key}")
    private String serviceKey;

    // Getters
    public String getSupabaseUrl() {
        return supabaseUrl;
    }

    public String getAnonKey() {
        return anonKey;
    }

    public String getServiceKey() {
        return serviceKey;
    }

    /**
     * Validate Supabase configuration
     * Called on application startup
     */
    public void validate() {
        if (supabaseUrl == null || supabaseUrl.isEmpty()) {
            throw new IllegalStateException("SUPABASE_URL is not configured");
        }
        if (anonKey == null || anonKey.isEmpty()) {
            throw new IllegalStateException("SUPABASE_ANON_KEY is not configured");
        }
        if (serviceKey == null || serviceKey.isEmpty()) {
            throw new IllegalStateException("SUPABASE_SERVICE_KEY is not configured");
        }
    }
}
