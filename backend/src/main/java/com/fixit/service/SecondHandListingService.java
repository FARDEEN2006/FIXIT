package com.fixit.service;

import com.fixit.dto.SecondHandListingRequest;
import com.fixit.dto.SecondHandListingResponse;
import com.fixit.dto.SecondHandImageResponse;
import com.fixit.util.ImageCompressionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

/**
 * Second-Hand Listing Service
 * 
 * Handles:
 * - Second-hand listing CRUD
 * - Image upload and management (max 4 images)
 * - Email verification workflow
 * - Listing status tracking
 * - Admin operations
 * 
 * NOTE: Listings are PRIVATE and only accessible to authenticated admin users
 */
@Service
public class SecondHandListingService {

    private static final Logger logger = LoggerFactory.getLogger(SecondHandListingService.class);

    @Autowired
    private SupabaseService supabaseService;

    @Autowired
    private ImageCompressionUtil imageCompressionUtil;

    @Autowired
    private EmailService emailService;

    @Value("${image.max-secondhand-images:4}")
    private int maxImages;

    @Value("${app.whatsapp-number}")
    private String whatsappNumber;

    private static final String LISTINGS_TABLE = "second_hand_listings";
    private static final String IMAGES_TABLE = "second_hand_images";
    private static final String SECONDHAND_BUCKET = "second-hand";

    /**
     * Create new second-hand listing (Pre-verification)
     * Generates verification token and sends email
     */
    public Map<String, Object> createListingRequest(SecondHandListingRequest request) {
        logger.info("Creating second-hand listing request for: {}", request.getSellerEmail());
        
        // Generate verification token
        String verificationToken = generateVerificationToken();

        Map<String, Object> listingData = new HashMap<>();
        listingData.put("seller_name", request.getSellerName());
        listingData.put("seller_phone", request.getSellerPhone());
        listingData.put("seller_email", request.getSellerEmail());
        listingData.put("product_name", request.getProductName());
        listingData.put("condition", request.getCondition());
        listingData.put("detailed_description", request.getDetailedDescription());
        listingData.put("expected_price", request.getExpectedPrice());
        listingData.put("listing_status", "NEW");
        listingData.put("email_verified", false);
        listingData.put("email_verification_token", verificationToken);

        Map<String, Object> result = supabaseService.insertRecord(LISTINGS_TABLE, listingData);
        
        if (result != null) {
            String listingId = (String) result.get("id");
            logger.info("Listing created (pending verification): {}", listingId);
            
            // Send verification email
            try {
                emailService.sendVerificationEmail(
                    request.getSellerEmail(),
                    request.getSellerName(),
                    listingId,
                    verificationToken
                );
            } catch (Exception e) {
                logger.error("Failed to send verification email: {}", e.getMessage());
                // Listing is created but email failed - seller should receive it eventually or contact support
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("listing_id", listingId);
            response.put("message", "Verification email sent. Please check your email to proceed.");
            response.put("next_step", "verify_email");
            return response;
        }

        throw new RuntimeException("Failed to create listing");
    }

    /**
     * Verify seller email
     */
    public boolean verifySellerEmail(String listingId, String token) {
        logger.info("Verifying email for listing: {}", listingId);
        
        // Query listing to check token
        Map<String, String> filters = new HashMap<>();
        filters.put("id", "eq." + listingId);

        List<Map<String, Object>> results = supabaseService.queryTable(LISTINGS_TABLE, filters);
        
        if (results.isEmpty()) {
            logger.warn("Listing not found: {}", listingId);
            return false;
        }

        Map<String, Object> listing = results.get(0);
        String storedToken = (String) listing.get("email_verification_token");

        if (!token.equals(storedToken)) {
            logger.warn("Invalid verification token for listing: {}", listingId);
            return false;
        }

        // Update listing - mark as verified
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("email_verified", true);
        updateData.put("email_verification_token", null);
        updateData.put("email_verified_at", java.time.LocalDateTime.now());
        updateData.put("listing_status", "REVIEWING");

        if (supabaseService.updateRecord(LISTINGS_TABLE, listingId, updateData)) {
            logger.info("Email verified for listing: {}", listingId);
            return true;
        }

        return false;
    }

    /**
     * Add image to listing (max 4 images)
     */
    public SecondHandImageResponse addImage(String listingId, MultipartFile imageFile, boolean isThumbnail) throws IOException {
        logger.info("Adding image to listing: {} (thumbnail: {})", listingId, isThumbnail);
        
        // Get current image count
        Map<String, String> filters = new HashMap<>();
        filters.put("listing_id", "eq." + listingId);
        
        List<Map<String, Object>> existingImages = supabaseService.queryTable(IMAGES_TABLE, filters);
        
        if (existingImages.size() >= maxImages) {
            throw new IllegalArgumentException("Maximum " + maxImages + " images per listing exceeded");
        }

        // Compress image
        byte[] compressedImage = imageCompressionUtil.compressImage(imageFile);
        
        // Generate filename
        String filename = imageCompressionUtil.generateUniqueFilename(imageFile.getOriginalFilename());
        String imageOrder = String.valueOf(existingImages.size() + 1);
        String filePath = listingId + "/" + imageOrder + ".jpg";

        // Upload to storage
        String uploadedPath = supabaseService.uploadFile(SECONDHAND_BUCKET, filePath, compressedImage);
        
        if (uploadedPath != null) {
            // Create image record
            Map<String, Object> imageData = new HashMap<>();
            imageData.put("listing_id", listingId);
            imageData.put("storage_path", uploadedPath);
            imageData.put("image_order", Integer.parseInt(imageOrder));
            imageData.put("is_thumbnail", isThumbnail);

            Map<String, Object> result = supabaseService.insertRecord(IMAGES_TABLE, imageData);
            
            if (result != null) {
                logger.info("Image added to listing: {}", listingId);
                return mapToImageResponse(result);
            }
        }

        throw new RuntimeException("Failed to upload image");
    }

    /**
     * Get listing by ID (Admin only - returns full details including seller info)
     */
    public SecondHandListingResponse getListingById(String listingId) {
        logger.info("Fetching listing details: {}", listingId);
        
        Map<String, String> filters = new HashMap<>();
        filters.put("id", "eq." + listingId);

        List<Map<String, Object>> results = supabaseService.queryTable(LISTINGS_TABLE, filters);
        
        if (results.isEmpty()) {
            return null;
        }

        Map<String, Object> listing = results.get(0);
        SecondHandListingResponse response = mapToListingResponse(listing);

        // Get images
        Map<String, String> imageFilters = new HashMap<>();
        imageFilters.put("listing_id", "eq." + listingId);
        
        List<Map<String, Object>> images = supabaseService.queryTable(IMAGES_TABLE, imageFilters);
        List<SecondHandImageResponse> imageResponses = new ArrayList<>();
        
        for (Map<String, Object> image : images) {
            SecondHandImageResponse imageResponse = mapToImageResponse(image);
            imageResponses.add(imageResponse);
        }
        
        response.setImages(imageResponses);
        return response;
    }

    /**
     * Get all listings (Admin only - paginated)
     */
    public List<SecondHandListingResponse> getAllListings(String status, int page, int pageSize) {
        logger.info("Fetching all second-hand listings - status: {}, page: {}", status, page);
        
        Map<String, String> filters = new HashMap<>();
        if (status != null && !status.isEmpty()) {
            filters.put("listing_status", "eq." + status);
        }
        filters.put("order", "created_at.desc");
        filters.put("limit", String.valueOf(pageSize));
        filters.put("offset", String.valueOf(page * pageSize));

        List<Map<String, Object>> results = supabaseService.queryTable(LISTINGS_TABLE, filters);
        List<SecondHandListingResponse> listings = new ArrayList<>();
        
        for (Map<String, Object> row : results) {
            listings.add(mapToListingResponse(row));
        }

        return listings;
    }

    /**
     * Update listing status (Admin only)
     */
    public boolean updateListingStatus(String listingId, String newStatus) {
        logger.info("Updating listing status - ID: {}, new status: {}", listingId, newStatus);
        
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("listing_status", newStatus);

        return supabaseService.updateRecord(LISTINGS_TABLE, listingId, updateData);
    }

    /**
     * Delete listing (Admin only)
     */
    public boolean deleteListing(String listingId) {
        logger.info("Deleting listing: {}", listingId);
        
        // Delete all images first
        Map<String, String> filters = new HashMap<>();
        filters.put("listing_id", "eq." + listingId);
        
        List<Map<String, Object>> images = supabaseService.queryTable(IMAGES_TABLE, filters);
        
        for (Map<String, Object> image : images) {
            String imagePath = (String) image.get("storage_path");
            if (imagePath != null) {
                supabaseService.deleteFile(SECONDHAND_BUCKET, imagePath);
            }
            supabaseService.deleteRecord(IMAGES_TABLE, (String) image.get("id"));
        }

        // Delete listing
        return supabaseService.deleteRecord(LISTINGS_TABLE, listingId);
    }

    /**
     * Generate verification token
     */
    private String generateVerificationToken() {
        return UUID.randomUUID().toString() + System.currentTimeMillis();
    }

    /**
     * Map database row to SecondHandListingResponse
     */
    private SecondHandListingResponse mapToListingResponse(Map<String, Object> row) {
        return SecondHandListingResponse.builder()
                .id((String) row.get("id"))
                .sellerName((String) row.get("seller_name"))
                .sellerPhone((String) row.get("seller_phone"))
                .sellerEmail((String) row.get("seller_email"))
                .productName((String) row.get("product_name"))
                .condition((String) row.get("condition"))
                .detailedDescription((String) row.get("detailed_description"))
                .expectedPrice(new java.math.BigDecimal(row.get("expected_price").toString()))
                .listingStatus((String) row.get("listing_status"))
                .emailVerified((Boolean) row.get("email_verified"))
                .emailVerifiedAt(parseTimestamp((String) row.get("email_verified_at")))
                .createdAt(parseTimestamp((String) row.get("created_at")))
                .updatedAt(parseTimestamp((String) row.get("updated_at")))
                .build();
    }

    /**
     * Map database row to SecondHandImageResponse
     */
    private SecondHandImageResponse mapToImageResponse(Map<String, Object> row) {
        String storagePath = (String) row.get("storage_path");
        String imageUrl = supabaseService.generateSignedUrl(SECONDHAND_BUCKET, storagePath, 3600); // 1 hour expiry
        
        return SecondHandImageResponse.builder()
                .id((String) row.get("id"))
                .storagePath(storagePath)
                .imageUrl(imageUrl)
                .imageOrder((Integer) row.get("image_order"))
                .isThumbnail((Boolean) row.get("is_thumbnail"))
                .createdAt(parseTimestamp((String) row.get("created_at")))
                .build();
    }

    /**
     * Parse timestamp
     */
    private java.time.LocalDateTime parseTimestamp(String timestamp) {
        if (timestamp == null) return null;
        return java.time.LocalDateTime.parse(timestamp, java.time.format.DateTimeFormatter.ISO_DATE_TIME);
    }
}
