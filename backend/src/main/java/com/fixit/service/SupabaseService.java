package com.fixit.service;

import com.fixit.config.SupabaseConfig;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.*;

/**
 * Supabase Service
 * 
 * Handles:
 * - Database queries via PostgREST API
 * - Storage operations via Storage API
 * - File upload/download
 * - URL generation
 */
@Service
public class SupabaseService {

    private static final Logger logger = LoggerFactory.getLogger(SupabaseService.class);

    @Autowired
    private SupabaseConfig supabaseConfig;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.service-key}")
    private String serviceKey;

    /**
     * Call Supabase RPC function
     */
    public Map<String, Object> callRpcFunction(String functionName, Map<String, Object> params) {
        try {
            String url = supabaseUrl + "/rest/v1/rpc/" + functionName;
            
            // Make request with service key
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("Authorization", "Bearer " + serviceKey);
            headers.set("Content-Type", "application/json");
            headers.set("Prefer", "return=representation");

            org.springframework.http.HttpEntity<Map<String, Object>> request = 
                    new org.springframework.http.HttpEntity<>(params, headers);

            org.springframework.http.ResponseEntity<Map> response = 
                    restTemplate.postForEntity(url, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("RPC function {} called successfully", functionName);
                return response.getBody();
            } else {
                logger.error("RPC function {} failed with status {}", functionName, response.getStatusCode());
                return null;
            }
        } catch (Exception e) {
            logger.error("Error calling RPC function {}: {}", functionName, e.getMessage());
            return null;
        }
    }

    /**
     * Query Supabase table
     */
    public List<Map<String, Object>> queryTable(String tableName, Map<String, String> filters) {
        try {
            String url = supabaseUrl + "/rest/v1/" + tableName;
            
            // Build query string
            StringBuilder queryBuilder = new StringBuilder(url);
            if (filters != null && !filters.isEmpty()) {
                queryBuilder.append("?");
                filters.forEach((key, value) -> 
                    queryBuilder.append(key).append("=").append(value).append("&")
                );
            }

            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("Authorization", "Bearer " + serviceKey);
            headers.set("Content-Type", "application/json");

            org.springframework.http.HttpEntity<String> request = 
                    new org.springframework.http.HttpEntity<>("", headers);

            org.springframework.http.ResponseEntity<List> response = 
                    restTemplate.exchange(queryBuilder.toString(), org.springframework.http.HttpMethod.GET, 
                            request, List.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("Query table {} successful", tableName);
                return response.getBody() != null ? response.getBody() : new ArrayList<>();
            }
            return new ArrayList<>();
        } catch (Exception e) {
            logger.error("Error querying table {}: {}", tableName, e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Insert record into table
     */
    public Map<String, Object> insertRecord(String tableName, Map<String, Object> data) {
        try {
            String url = supabaseUrl + "/rest/v1/" + tableName;
            
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("Authorization", "Bearer " + serviceKey);
            headers.set("Content-Type", "application/json");
            headers.set("Prefer", "return=representation");

            org.springframework.http.HttpEntity<Map<String, Object>> request = 
                    new org.springframework.http.HttpEntity<>(data, headers);

            org.springframework.http.ResponseEntity<List> response = 
                    restTemplate.postForEntity(url, request, List.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null && !response.getBody().isEmpty()) {
                logger.info("Record inserted into table {}", tableName);
                return (Map<String, Object>) response.getBody().get(0);
            }
            return null;
        } catch (Exception e) {
            logger.error("Error inserting record into table {}: {}", tableName, e.getMessage());
            return null;
        }
    }

    /**
     * Update record in table
     */
    public boolean updateRecord(String tableName, String id, Map<String, Object> data) {
        try {
            String url = supabaseUrl + "/rest/v1/" + tableName + "?id=eq." + id;
            
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("Authorization", "Bearer " + serviceKey);
            headers.set("Content-Type", "application/json");

            org.springframework.http.HttpEntity<Map<String, Object>> request = 
                    new org.springframework.http.HttpEntity<>(data, headers);

            org.springframework.http.ResponseEntity<Void> response = 
                    restTemplate.exchange(url, org.springframework.http.HttpMethod.PATCH, 
                            request, Void.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("Record updated in table {}", tableName);
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.error("Error updating record in table {}: {}", tableName, e.getMessage());
            return false;
        }
    }

    /**
     * Delete record from table
     */
    public boolean deleteRecord(String tableName, String id) {
        try {
            String url = supabaseUrl + "/rest/v1/" + tableName + "?id=eq." + id;
            
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("Authorization", "Bearer " + serviceKey);

            org.springframework.http.HttpEntity<String> request = 
                    new org.springframework.http.HttpEntity<>("", headers);

            org.springframework.http.ResponseEntity<Void> response = 
                    restTemplate.exchange(url, org.springframework.http.HttpMethod.DELETE, 
                            request, Void.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("Record deleted from table {}", tableName);
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.error("Error deleting record from table {}: {}", tableName, e.getMessage());
            return false;
        }
    }

    /**
     * Upload file to storage bucket
     */
    public String uploadFile(String bucketName, String filePath, byte[] fileContent) {
        try {
            String url = supabaseUrl + "/storage/v1/object/" + bucketName + "/" + filePath;
            
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("Authorization", "Bearer " + serviceKey);
            headers.set("Content-Type", "image/jpeg");

            org.springframework.http.HttpEntity<byte[]> request = 
                    new org.springframework.http.HttpEntity<>(fileContent, headers);

            org.springframework.http.ResponseEntity<Map> response = 
                    restTemplate.postForEntity(url, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("File uploaded to bucket {}: {}", bucketName, filePath);
                return filePath;
            }
            logger.error("File upload failed with status: {}", response.getStatusCode());
            return null;
        } catch (Exception e) {
            logger.error("Error uploading file to storage: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Generate public URL for file in public bucket
     */
    public String generatePublicUrl(String bucketName, String filePath) {
        // Format: https://PROJECT_ID.supabase.co/storage/v1/object/public/BUCKET/PATH
        return supabaseUrl + "/storage/v1/object/public/" + bucketName + "/" + filePath;
    }

    /**
     * Generate signed URL for private bucket
     */
    public String generateSignedUrl(String bucketName, String filePath, long expirationSeconds) {
        try {
            String url = supabaseUrl + "/storage/v1/object/sign/" + bucketName + "/" + filePath;
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("expiresIn", expirationSeconds);

            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("Authorization", "Bearer " + serviceKey);
            headers.set("Content-Type", "application/json");

            org.springframework.http.HttpEntity<Map<String, Object>> request = 
                    new org.springframework.http.HttpEntity<>(requestBody, headers);

            org.springframework.http.ResponseEntity<Map> response = 
                    restTemplate.postForEntity(url, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String signedPath = (String) response.getBody().get("signedURL");
                logger.info("Signed URL generated for file: {}", filePath);
                return signedPath;
            }
            return null;
        } catch (Exception e) {
            logger.error("Error generating signed URL: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Delete file from storage
     */
    public boolean deleteFile(String bucketName, String filePath) {
        try {
            String url = supabaseUrl + "/storage/v1/object/" + bucketName + "/" + filePath;
            
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("Authorization", "Bearer " + serviceKey);

            org.springframework.http.HttpEntity<String> request = 
                    new org.springframework.http.HttpEntity<>("", headers);

            org.springframework.http.ResponseEntity<Void> response = 
                    restTemplate.exchange(url, org.springframework.http.HttpMethod.DELETE, 
                            request, Void.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("File deleted from bucket {}: {}", bucketName, filePath);
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.error("Error deleting file from storage: {}", e.getMessage());
            return false;
        }
    }
}
