package com.fixit.controller;

import com.fixit.dto.SecondHandListingRequest;
import com.fixit.dto.SecondHandListingResponse;
import com.fixit.dto.SecondHandImageResponse;
import com.fixit.dto.EmailVerificationRequest;
import com.fixit.dto.EmailVerificationResponse;
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
 * - POST   /api/sell/verify/request          (public - create listing request)
 * - POST   /api/sell/verify/confirm          (public - verify email with token)
 * - POST   /api/sell/listings/{id}/images    (public - add images to listing)
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

    /**
     * Create second-hand listing request (Public)
     * POST /api/sell/verify/request
     * 
     * Initiates listing process and sends verification email
     */
    @PostMapping("/sell/verify/request")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createListingRequest(
            @Valid @RequestBody SecondHandListingRequest request) {
        try {
            logger.info("Creating second-hand listing request for: {}", request.getSellerEmail());
            
            Map<String, Object> result = listingService.createListingRequest(request);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Verification email sent. Please check your email.", result));
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid listing data: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            logger.error("Error creating listing request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Error creating listing request", null));
        }
    }

    /**
     * Verify seller email (Public)
     * POST /api/sell/verify/confirm
     * 
     * Verifies seller email using token from email link
     */
    @PostMapping("/sell/verify/confirm")
    public ResponseEntity<ApiResponse<EmailVerificationResponse>> verifyEmail(
            @Valid @RequestBody EmailVerificationRequest request) {
        try {
            logger.info("Verifying email for listing: {}", request.getListingId());
            
            boolean verified = listingService.verifySellerEmail(request.getListingId(), request.getToken());
            
            if (!verified) {
                EmailVerificationResponse response = new EmailVerificationResponse();
                response.setSuccess(false);
                response.setMessage("Invalid or expired verification link");
                response.setListingId(request.getListingId());
                response.setNextStep(null);
                
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, "Verification failed", response));
            }
            
            EmailVerificationResponse response = new EmailVerificationResponse();
            response.setSuccess(true);
            response.setMessage("Email verified successfully");
            response.setListingId(request.getListingId());
            response.setNextStep("upload_images");
            
            return ResponseEntity.ok(
                new ApiResponse<>(true, "Email verified successfully", response)
            );
        } catch (Exception e) {
            logger.error("Error verifying email: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Error verifying email", null));
        }
    }

    /**
     * Add image to listing (Public - during verification process)
     * POST /api/sell/listings/{id}/images
     * 
     * Allows seller to add up to 4 images during listing creation
     */
    @PostMapping("/sell/listings/{id}/images")
    public ResponseEntity<ApiResponse<SecondHandImageResponse>> addListingImage(
            @PathVariable String id,
            @RequestParam("image") MultipartFile imageFile,
            @RequestParam(defaultValue = "false") boolean isThumbnail,
            @RequestParam("verificationToken") String verificationToken) {
        try {
            logger.info("Adding image to listing: {}", id);
            
            if (!listingService.isVerifiedUploadToken(id, verificationToken)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>(false, "A valid verified upload token is required", null));
            }
            // Validate image
            imageCompressionUtil.validateImage(imageFile);
            
            SecondHandImageResponse imageResponse = listingService.addImage(id, imageFile, isThumbnail);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Image added successfully", imageResponse));
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid image: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (IOException e) {
            logger.error("IO error uploading image: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Error processing image", null));
        } catch (Exception e) {
            logger.error("Error adding image: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Error adding image", null));
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
