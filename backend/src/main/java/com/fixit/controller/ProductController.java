package com.fixit.controller;

import com.fixit.dto.ProductRequest;
import com.fixit.dto.ProductResponse;
import com.fixit.dto.ProductDetailResponse;
import com.fixit.dto.ProductListResponse;
import com.fixit.dto.ProductImageUploadResponse;
import com.fixit.dto.ApiResponse;
import com.fixit.dto.ApiErrorResponse;
import com.fixit.service.ProductService;
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
import java.util.List;

/**
 * Product Controller
 * 
 * Handles product-related endpoints:
 * - GET  /api/products                 (public - list all active)
 * - GET  /api/products/{id}            (public - single product details)
 * - POST /api/admin/products           (admin - create product)
 * - PUT  /api/admin/products/{id}      (admin - update product)
 * - DELETE /api/admin/products/{id}    (admin - delete product)
 * - POST /api/admin/products/{id}/image (admin - upload product image)
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    @Autowired
    private ProductService productService;

    @Autowired
    private ImageCompressionUtil imageCompressionUtil;

    /**
     * Get all active products
     * GET /api/products
     * Query params: page=0, pageSize=10
     */
    @GetMapping
    public ResponseEntity<ApiResponse<ProductListResponse>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        try {
            logger.info("Fetching all products - page: {}, pageSize: {}", page, pageSize);
            
            List<ProductResponse> products = productService.getAllActiveProducts(page, pageSize);
            
            ProductListResponse response = new ProductListResponse();
            response.setPage(page);
            response.setPageSize(pageSize);
            response.setTotalCount((long) products.size());
            response.setProducts(products);
            
            return ResponseEntity.ok(
                new ApiResponse<>(true, "Products fetched successfully", response)
            );
        } catch (Exception e) {
            logger.error("Error fetching products: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Error fetching products", null));
        }
    }

    /**
     * Get product by ID
     * GET /api/products/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> getProductById(@PathVariable String id) {
        try {
            logger.info("Fetching product: {}", id);
            
            ProductDetailResponse product = productService.getProductById(id);
            
            if (product == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "Product not found", null));
            }
            
            return ResponseEntity.ok(
                new ApiResponse<>(true, "Product fetched successfully", product)
            );
        } catch (Exception e) {
            logger.error("Error fetching product: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Error fetching product", null));
        }
    }

    /**
     * Create new product (Admin only)
     * POST /api/admin/products
     */
    @PostMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(@Valid @RequestBody ProductRequest request) {
        try {
            logger.info("Creating product: {}", request.getName());
            
            ProductResponse product = productService.createProduct(request);
            
            if (product == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to create product", null));
            }
            
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Product created successfully", product));
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid product data: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            logger.error("Error creating product: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Error creating product", null));
        }
    }

    /**
     * Update product (Admin only)
     * PUT /api/admin/products/{id}
     */
    @PutMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable String id,
            @Valid @RequestBody ProductRequest request) {
        try {
            logger.info("Updating product: {}", id);
            
            ProductResponse product = productService.updateProduct(id, request);
            
            if (product == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "Product not found", null));
            }
            
            return ResponseEntity.ok(
                new ApiResponse<>(true, "Product updated successfully", product)
            );
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid product data: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            logger.error("Error updating product: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Error updating product", null));
        }
    }

    /**
     * Delete product (Admin only)
     * DELETE /api/admin/products/{id}
     */
    @DeleteMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable String id) {
        try {
            logger.info("Deleting product: {}", id);
            
            boolean deleted = productService.deleteProduct(id);
            
            if (!deleted) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "Product not found", null));
            }
            
            return ResponseEntity.ok(
                new ApiResponse<>(true, "Product deleted successfully", null)
            );
        } catch (Exception e) {
            logger.error("Error deleting product: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Error deleting product", null));
        }
    }

    /**
     * Upload product image (Admin only)
     * POST /api/admin/products/{id}/image
     */
    @PostMapping("/admin/{id}/image")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductImageUploadResponse>> uploadProductImage(
            @PathVariable String id,
            @RequestParam("image") MultipartFile imageFile) {
        try {
            logger.info("Uploading image for product: {}", id);
            
            // Validate image
            imageCompressionUtil.validateImage(imageFile);
            
            String imageUrl = productService.uploadProductImage(id, imageFile);
            
            ProductImageUploadResponse response = new ProductImageUploadResponse();
            response.setProductId(id);
            response.setImageUrl(imageUrl);
            response.setMessage("Image uploaded successfully");
            
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Image uploaded successfully", response));
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid image: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (IOException e) {
            logger.error("IO error uploading image: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Error processing image", null));
        } catch (Exception e) {
            logger.error("Error uploading image: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Error uploading image", null));
        }
    }
}
