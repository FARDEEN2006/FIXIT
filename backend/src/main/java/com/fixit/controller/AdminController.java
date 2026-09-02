package com.fixit.controller;

import com.fixit.dto.StoreInformationRequest;
import com.fixit.dto.StoreInformationResponse;
import com.fixit.dto.ServiceRequest;
import com.fixit.dto.ServiceResponse;
import com.fixit.dto.ServiceListResponse;
import com.fixit.dto.ApiResponse;
import com.fixit.service.StoreService;
import com.fixit.service.ServiceService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Admin Controller
 * 
 * Handles admin-only operations:
 * - Store Information Management:
 *   GET    /api/store-info           (public read)
 *   PUT    /api/admin/store-info     (admin write)
 * 
 * - Services Management:
 *   GET    /api/services             (public - list)
 *   GET    /api/services/{id}        (public - get service)
 *   POST   /api/admin/services       (admin - create)
 *   PUT    /api/admin/services/{id}  (admin - update)
 *   DELETE /api/admin/services/{id}  (admin - delete)
 */
@RestController
@RequestMapping("/api")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private StoreService storeService;

    @Autowired
    private ServiceService serviceService;

    // ==================== Store Information Endpoints ====================

    /**
     * Get store information (Public read)
     * GET /api/store-info
     */
    @GetMapping("/store-info")
    public ResponseEntity<ApiResponse<StoreInformationResponse>> getStoreInfo() {
        try {
            logger.info("Fetching store information");
            
            StoreInformationResponse storeInfo = storeService.getStoreInfo();
            
            if (storeInfo == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "Store information not found", null));
            }
            
            return ResponseEntity.ok(
                new ApiResponse<>(true, "Store information fetched successfully", storeInfo)
            );
        } catch (Exception e) {
            logger.error("Error fetching store information: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Error fetching store information", null));
        }
    }

    /**
     * Update store information (Admin only)
     * PUT /api/admin/store-info
     */
    @PutMapping("/admin/store-info")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<StoreInformationResponse>> updateStoreInfo(
            @Valid @RequestBody StoreInformationRequest request) {
        try {
            logger.info("Updating store information");
            
            StoreInformationResponse storeInfo = storeService.updateStoreInfo(request);
            
            if (storeInfo == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to update store information", null));
            }
            
            return ResponseEntity.ok(
                new ApiResponse<>(true, "Store information updated successfully", storeInfo)
            );
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid store information: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            logger.error("Error updating store information: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Error updating store information", null));
        }
    }

    // ==================== Services Endpoints ====================

    /**
     * Get all services (Public read)
     * GET /api/services
     */
    @GetMapping("/services")
    public ResponseEntity<ApiResponse<ServiceListResponse>> getAllServices() {
        try {
            logger.info("Fetching all services");
            
            ServiceListResponse services = serviceService.getAllServices();
            
            return ResponseEntity.ok(
                new ApiResponse<>(true, "Services fetched successfully", services)
            );
        } catch (Exception e) {
            logger.error("Error fetching services: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Error fetching services", null));
        }
    }

    /**
     * Get service by ID (Public read)
     * GET /api/services/{id}
     */
    @GetMapping("/services/{id}")
    public ResponseEntity<ApiResponse<ServiceResponse>> getServiceById(@PathVariable String id) {
        try {
            logger.info("Fetching service: {}", id);
            
            ServiceResponse service = serviceService.getServiceById(id);
            
            if (service == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "Service not found", null));
            }
            
            return ResponseEntity.ok(
                new ApiResponse<>(true, "Service fetched successfully", service)
            );
        } catch (Exception e) {
            logger.error("Error fetching service: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Error fetching service", null));
        }
    }

    /**
     * Create service (Admin only)
     * POST /api/admin/services
     */
    @PostMapping("/admin/services")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ServiceResponse>> createService(
            @Valid @RequestBody ServiceRequest request) {
        try {
            logger.info("Creating service: {}", request.getServiceName());
            
            ServiceResponse service = serviceService.createService(request);
            
            if (service == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to create service", null));
            }
            
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Service created successfully", service));
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid service data: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            logger.error("Error creating service: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Error creating service", null));
        }
    }

    /**
     * Update service (Admin only)
     * PUT /api/admin/services/{id}
     */
    @PutMapping("/admin/services/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ServiceResponse>> updateService(
            @PathVariable String id,
            @Valid @RequestBody ServiceRequest request) {
        try {
            logger.info("Updating service: {}", id);
            
            ServiceResponse service = serviceService.updateService(id, request);
            
            if (service == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "Service not found", null));
            }
            
            return ResponseEntity.ok(
                new ApiResponse<>(true, "Service updated successfully", service)
            );
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid service data: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            logger.error("Error updating service: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Error updating service", null));
        }
    }

    /**
     * Delete service (Admin only)
     * DELETE /api/admin/services/{id}
     */
    @DeleteMapping("/admin/services/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteService(@PathVariable String id) {
        try {
            logger.info("Deleting service: {}", id);
            
            boolean deleted = serviceService.deleteService(id);
            
            if (!deleted) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "Service not found", null));
            }
            
            return ResponseEntity.ok(
                new ApiResponse<>(true, "Service deleted successfully", null)
            );
        } catch (Exception e) {
            logger.error("Error deleting service: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Error deleting service", null));
        }
    }
}
