package com.fixit.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Storage Service
 * 
 * Handles:
 * - File upload wrapper
 * - URL generation (public and signed)
 * - File deletion
 * - Storage management
 */
@Service
public class StorageService {

    private static final Logger logger = LoggerFactory.getLogger(StorageService.class);

    @Autowired
    private SupabaseService supabaseService;

    /**
     * Upload file to public bucket
     */
    public String uploadPublicFile(String bucketName, String filePath, byte[] fileContent) {
        logger.info("Uploading public file: {}/{}", bucketName, filePath);
        String uploadedPath = supabaseService.uploadFile(bucketName, filePath, fileContent);
        
        if (uploadedPath != null) {
            return supabaseService.generatePublicUrl(bucketName, uploadedPath);
        }
        return null;
    }

    /**
     * Upload file to private bucket
     */
    public String uploadPrivateFile(String bucketName, String filePath, byte[] fileContent) {
        logger.info("Uploading private file: {}/{}", bucketName, filePath);
        String uploadedPath = supabaseService.uploadFile(bucketName, filePath, fileContent);
        
        if (uploadedPath != null) {
            // Return signed URL for private access (1 hour expiry)
            return supabaseService.generateSignedUrl(bucketName, uploadedPath, 3600);
        }
        return null;
    }

    /**
     * Get public URL for file
     */
    public String getPublicUrl(String bucketName, String filePath) {
        return supabaseService.generatePublicUrl(bucketName, filePath);
    }

    /**
     * Get signed URL for file (temporary access)
     */
    public String getSignedUrl(String bucketName, String filePath, long expirationSeconds) {
        return supabaseService.generateSignedUrl(bucketName, filePath, expirationSeconds);
    }

    /**
     * Delete file from storage
     */
    public boolean deleteFile(String bucketName, String filePath) {
        logger.info("Deleting file: {}/{}", bucketName, filePath);
        return supabaseService.deleteFile(bucketName, filePath);
    }
}
