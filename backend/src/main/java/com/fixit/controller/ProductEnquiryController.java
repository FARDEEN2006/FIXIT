package com.fixit.controller;

import com.fixit.dto.ProductEnquiryRequest;
import com.fixit.dto.ProductEnquiryResponse;
import com.fixit.dto.ProductEnquiryListResponse;
import com.fixit.dto.ProductEnquiryStatusUpdate;
import com.fixit.dto.ApiResponse;
import com.fixit.service.ProductEnquiryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Product Enquiry Controller
 * 
 * Handles product enquiry endpoints:
 * - POST   /api/product-enquiries              (public - create enquiry)
 * - GET    /api/admin/product-enquiries        (admin - list all enquiries)
 * - GET    /api/admin/product-enquiries/{id}   (admin - get enquiry details)
 * - PUT    /api/admin/product-enquiries/{id}/status (admin - update status)
 * - DELETE /api/admin/product-enquiries/{id}   (admin - delete enquiry)
 * - GET    /api/admin/products/{id}/enquiries  (admin - list product enquiries)
 */
@RestController
@RequestMapping("/api")
public class ProductEnquiryController {

    private static final Logger logger = LoggerFactory.getLogger(ProductEnquiryController.class);

    @Autowired
    private ProductEnquiryService enquiryService;

    /**
     * Create product enquiry (Public)
     * POST /api/product-enquiries
     */
    @PostMapping("/product-enquiries")
    public ResponseEntity<ApiResponse<ProductEnquiryResponse>> createEnquiry(
            @Valid @RequestBody ProductEnquiryRequest request) {
        try {
            logger.info("Creating product enquiry for product: {}", request.getProductId());
            
            ProductEnquiryResponse enquiry = enquiryService.createEnquiry(request);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Enquiry created successfully. We'll contact you soon via WhatsApp.", enquiry));
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid enquiry data: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            logger.error("Error creating enquiry: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Error creating enquiry", null));
        }
    }

    /**
     * Get all enquiries (Admin only)
     * GET /api/admin/product-enquiries?status=NEW&page=0&pageSize=10
     */
    @GetMapping("/admin/product-enquiries")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductEnquiryListResponse>> getAllEnquiries(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        try {
            logger.info("Fetching all enquiries - status: {}, page: {}", status, page);
            
            List<ProductEnquiryResponse> enquiries = enquiryService.getAllEnquiries(status, page, pageSize);
            
            ProductEnquiryListResponse response = new ProductEnquiryListResponse();
            response.setPage(page);
            response.setPageSize(pageSize);
            response.setTotalCount((long) enquiries.size());
            response.setEnquiries(enquiries);
            
            return ResponseEntity.ok(
                new ApiResponse<>(true, "Enquiries fetched successfully", response)
            );
        } catch (Exception e) {
            logger.error("Error fetching enquiries: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Error fetching enquiries", null));
        }
    }

    /**
     * Get enquiry by ID (Admin only)
     * GET /api/admin/product-enquiries/{id}
     */
    @GetMapping("/admin/product-enquiries/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductEnquiryResponse>> getEnquiryById(@PathVariable String id) {
        try {
            logger.info("Fetching enquiry: {}", id);
            
            ProductEnquiryResponse enquiry = enquiryService.getEnquiryById(id);
            
            if (enquiry == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "Enquiry not found", null));
            }
            
            return ResponseEntity.ok(
                new ApiResponse<>(true, "Enquiry fetched successfully", enquiry)
            );
        } catch (Exception e) {
            logger.error("Error fetching enquiry: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Error fetching enquiry", null));
        }
    }

    /**
     * Update enquiry status (Admin only)
     * PUT /api/admin/product-enquiries/{id}/status
     */
    @PutMapping("/admin/product-enquiries/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductEnquiryResponse>> updateEnquiryStatus(
            @PathVariable String id,
            @Valid @RequestBody ProductEnquiryStatusUpdate update) {
        try {
            logger.info("Updating enquiry status - ID: {}, new status: {}", id, update.getStatus());
            
            boolean updated = enquiryService.updateEnquiryStatus(id, update.getStatus());
            
            if (!updated) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "Enquiry not found", null));
            }
            
            ProductEnquiryResponse enquiry = enquiryService.getEnquiryById(id);
            
            return ResponseEntity.ok(
                new ApiResponse<>(true, "Enquiry status updated successfully", enquiry)
            );
        } catch (Exception e) {
            logger.error("Error updating enquiry status: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Error updating enquiry status", null));
        }
    }

    /**
     * Delete enquiry (Admin only)
     * DELETE /api/admin/product-enquiries/{id}
     */
    @DeleteMapping("/admin/product-enquiries/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteEnquiry(@PathVariable String id) {
        try {
            logger.info("Deleting enquiry: {}", id);
            
            boolean deleted = enquiryService.deleteEnquiry(id);
            
            if (!deleted) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "Enquiry not found", null));
            }
            
            return ResponseEntity.ok(
                new ApiResponse<>(true, "Enquiry deleted successfully", null)
            );
        } catch (Exception e) {
            logger.error("Error deleting enquiry: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Error deleting enquiry", null));
        }
    }

    /**
     * Get enquiries for a specific product (Admin only)
     * GET /api/admin/products/{productId}/enquiries?page=0&pageSize=10
     */
    @GetMapping("/admin/products/{productId}/enquiries")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductEnquiryListResponse>> getProductEnquiries(
            @PathVariable String productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        try {
            logger.info("Fetching enquiries for product: {} - page: {}", productId, page);
            
            List<ProductEnquiryResponse> enquiries = enquiryService.getProductEnquiries(productId, page, pageSize);
            
            ProductEnquiryListResponse response = new ProductEnquiryListResponse();
            response.setPage(page);
            response.setPageSize(pageSize);
            response.setTotalCount((long) enquiries.size());
            response.setEnquiries(enquiries);
            
            return ResponseEntity.ok(
                new ApiResponse<>(true, "Product enquiries fetched successfully", response)
            );
        } catch (Exception e) {
            logger.error("Error fetching product enquiries: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Error fetching product enquiries", null));
        }
    }
}
