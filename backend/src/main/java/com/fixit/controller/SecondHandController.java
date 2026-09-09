package com.fixit.controller;

import com.fixit.dto.SecondHandListingRequest;
import com.fixit.dto.SecondHandListingResponse;
import com.fixit.dto.SecondHandImageResponse;
import com.fixit.dto.ApiResponse;
import com.fixit.service.SecondHandListingService;
import com.fixit.util.ImageCompressionUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Second-Hand Listing Controller
 * 
 * Handles second-hand marketplace endpoints:
 * - POST   /api/sell/listings                (public - create private listing)
 * - GET    /api/admin/second-hand            (admin - list all listings)
 * - GET    /api/admin/second-hand/{id}       (admin - get listing details)
 * - PUT    /api/admin/second-hand/{id}/status (admin - update listing status)
 * - DELETE /api/admin/second-hand/{id}       (admin - delete listing)
 * 
 * NOTE: Listings are private and only accessible to authenticated admin users
 */
@RestController
@RequestMapping("/api")
public class SecondHandController {

    private static final Logger logger = LoggerFactory.getLogger(SecondHandController.class);

    @Autowired
    private SecondHandListingService listingService;

    @Autowired
    private ImageCompressionUtil imageCompressionUtil;

    /** Creates a private listing and accepts one required thumbnail plus up to three gallery images. */
    @PostMapping(value = "/sell/listings", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<SecondHandListingResponse>> createListing(
            @RequestParam("sellerName") String sellerName, @RequestParam("sellerPhone") String sellerPhone,
            @RequestParam(value = "sellerEmail", required = false) String sellerEmail, @RequestParam("productName") String productName,
            @RequestParam("condition") String condition, @RequestParam("detailedDescription") String detailedDescription,
            @RequestParam("expectedPrice") java.math.BigDecimal expectedPrice, @RequestParam("images") List<MultipartFile> images,
            @RequestHeader(value = "X-Client-Compressed", defaultValue = "false") boolean clientCompressed) {
        try {
            if (images.isEmpty() || images.size() > 4) throw new IllegalArgumentException("Provide between 1 and 4 images");
            for (MultipartFile image : images) imageCompressionUtil.validateImage(image);
            SecondHandListingRequest request = SecondHandListingRequest.builder().sellerName(sellerName).sellerPhone(sellerPhone).sellerEmail(sellerEmail).productName(productName).condition(condition).detailedDescription(detailedDescription).expectedPrice(expectedPrice).build();
            SecondHandListingResponse result = listingService.createListing(request, images, clientCompressed);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Listing submitted successfully", result));
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid listing data: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            logger.error("Error creating listing request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Error creating listing", null));
        }
    }

    /**
     * Get all second-hand listings (Admin only)
     * GET /api/admin/second-hand?status=NEW&page=0&pageSize=10
     * 
     * Returns only private seller information (names, contact details, etc.)
     */
    @GetMapping("/admin/second-hand")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAllListings(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        try {
            logger.info("Fetching all second-hand listings - status: {}, page: {}", status, page);
            
            List<SecondHandListingResponse> listings = listingService.getAllListings(status, page, pageSize);
            
            Map<String, Object> response = new HashMap<>();
            response.put("page", page);
            response.put("pageSize", pageSize);
            response.put("totalCount", listings.size());
            response.put("listings", listings);
            
            return ResponseEntity.ok(
                new ApiResponse<>(true, "Listings fetched successfully", response)
            );
        } catch (Exception e) {
            logger.error("Error fetching listings: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Error fetching listings", null));
        }
    }

    /**
     * Get listing by ID (Admin only)
     * GET /api/admin/second-hand/{id}
     * 
     * Returns complete listing with images and seller contact information
     */
    @GetMapping("/admin/second-hand/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SecondHandListingResponse>> getListingById(@PathVariable String id) {
        try {
            logger.info("Fetching listing: {}", id);
            
            SecondHandListingResponse listing = listingService.getListingById(id);
            
            if (listing == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "Listing not found", null));
            }
            
            return ResponseEntity.ok(
                new ApiResponse<>(true, "Listing fetched successfully", listing)
            );
        } catch (Exception e) {
            logger.error("Error fetching listing: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Error fetching listing", null));
        }
    }

    /**
     * Update listing status (Admin only)
     * PUT /api/admin/second-hand/{id}/status
     * 
     * Changes listing status: NEW → REVIEWING → CONTACTED → ACCEPTED/REJECTED/COMPLETED
     */
    @PutMapping("/admin/second-hand/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SecondHandListingResponse>> updateListingStatus(
            @PathVariable String id,
            @RequestBody Map<String, String> statusUpdate) {
        try {
            String newStatus = statusUpdate.get("status");
            
            if (newStatus == null || newStatus.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, "Status is required", null));
            }
            
            logger.info("Updating listing status - ID: {}, new status: {}", id, newStatus);
            
            boolean updated = listingService.updateListingStatus(id, newStatus);
            
            if (!updated) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "Listing not found", null));
            }
            
            SecondHandListingResponse listing = listingService.getListingById(id);
            
            return ResponseEntity.ok(
                new ApiResponse<>(true, "Listing status updated successfully", listing)
            );
        } catch (Exception e) {
            logger.error("Error updating listing status: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Error updating listing status", null));
        }
    }

    /**
     * Delete listing (Admin only)
     * DELETE /api/admin/second-hand/{id}
     * 
     * Deletes listing and all associated images
     */
    @DeleteMapping("/admin/second-hand/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteListing(@PathVariable String id) {
        try {
            logger.info("Deleting listing: {}", id);
            
            boolean deleted = listingService.deleteListing(id);
            
            if (!deleted) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "Listing not found", null));
            }
            
            return ResponseEntity.ok(
                new ApiResponse<>(true, "Listing deleted successfully", null)
            );
        } catch (Exception e) {
            logger.error("Error deleting listing: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Error deleting listing", null));
        }
    }
}
