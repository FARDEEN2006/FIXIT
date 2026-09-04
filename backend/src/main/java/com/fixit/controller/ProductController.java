package com.fixit.controller;

import com.fixit.dto.ProductRequest;
import com.fixit.dto.ProductResponse;
import com.fixit.dto.ProductDetailResponse;
import com.fixit.dto.ProductListResponse;
import com.fixit.dto.ProductImageUploadResponse;
import com.fixit.dto.ApiResponse;
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
 * Public:
 * GET /api/products
 * GET /api/products/{id}
 *
 * Admin:
 * GET    /api/products/admin
 * POST   /api/products/admin
 * PUT    /api/products/admin/{id}
 * DELETE /api/products/admin/{id}
 * POST   /api/products/admin/{id}/image
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private static final Logger logger =
            LoggerFactory.getLogger(ProductController.class);

    @Autowired
    private ProductService productService;

    @Autowired
    private ImageCompressionUtil imageCompressionUtil;

    /**
     * PUBLIC:
     * Get all active products.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<ProductListResponse>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize) {

        try {

            logger.info(
                    "Fetching active products - page: {}, pageSize: {}",
                    page,
                    pageSize
            );

            List<ProductResponse> products =
                    productService.getAllActiveProducts(
                            page,
                            pageSize
                    );

            ProductListResponse response =
                    new ProductListResponse();

            response.setPage(page);
            response.setPageSize(pageSize);
            response.setTotalCount(
                    (long) products.size()
            );
            response.setProducts(products);

            return ResponseEntity.ok(
                    new ApiResponse<>(
                            true,
                            "Products fetched successfully",
                            response
                    )
            );

        } catch (Exception e) {

            logger.error(
                    "Error fetching products: {}",
                    e.getMessage()
            );

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(
                            new ApiResponse<>(
                                    false,
                                    "Error fetching products",
                                    null
                            )
                    );
        }
    }

    /**
     * ADMIN:
     * Get ALL products.
     *
     * Includes both ACTIVE and INACTIVE products.
     */
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductListResponse>>
    getAllProductsForAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int pageSize) {

        try {

            logger.info(
                    "Admin fetching all products - page: {}, pageSize: {}",
                    page,
                    pageSize
            );

            List<ProductResponse> products =
                    productService.getAllProductsForAdmin(
                            page,
                            pageSize
                    );

            ProductListResponse response =
                    new ProductListResponse();

            response.setPage(page);
            response.setPageSize(pageSize);
            response.setTotalCount(
                    (long) products.size()
            );
            response.setProducts(products);

            return ResponseEntity.ok(
                    new ApiResponse<>(
                            true,
                            "All products fetched successfully",
                            response
                    )
            );

        } catch (Exception e) {

            logger.error(
                    "Error fetching admin products: {}",
                    e.getMessage()
            );

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(
                            new ApiResponse<>(
                                    false,
                                    "Error fetching admin products",
                                    null
                            )
                    );
        }
    }

    /**
     * PUBLIC:
     * Get product by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDetailResponse>>
    getProductById(
            @PathVariable String id) {

        try {

            logger.info(
                    "Fetching product: {}",
                    id
            );

            ProductDetailResponse product =
                    productService.getProductById(id);

            if (product == null) {

                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(
                                new ApiResponse<>(
                                        false,
                                        "Product not found",
                                        null
                                )
                        );
            }

            return ResponseEntity.ok(
                    new ApiResponse<>(
                            true,
                            "Product fetched successfully",
                            product
                    )
            );

        } catch (Exception e) {

            logger.error(
                    "Error fetching product: {}",
                    e.getMessage()
            );

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(
                            new ApiResponse<>(
                                    false,
                                    "Error fetching product",
                                    null
                            )
                    );
        }
    }

    /**
     * ADMIN:
     * Create product.
     */
    @PostMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductResponse>>
    createProduct(
            @Valid @RequestBody ProductRequest request) {

        try {

            logger.info(
                    "Creating product: {}",
                    request.getName()
            );

            ProductResponse product =
                    productService.createProduct(request);

            if (product == null) {

                return ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(
                                new ApiResponse<>(
                                        false,
                                        "Failed to create product",
                                        null
                                )
                        );
            }

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(
                            new ApiResponse<>(
                                    true,
                                    "Product created successfully",
                                    product
                            )
                    );

        } catch (IllegalArgumentException e) {

            logger.warn(
                    "Invalid product data: {}",
                    e.getMessage()
            );

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(
                            new ApiResponse<>(
                                    false,
                                    e.getMessage(),
                                    null
                            )
                    );

        } catch (Exception e) {

            logger.error(
                    "Error creating product: {}",
                    e.getMessage()
            );

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(
                            new ApiResponse<>(
                                    false,
                                    "Error creating product",
                                    null
                            )
                    );
        }
    }

    /**
     * ADMIN:
     * Update product.
     */
    @PutMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductResponse>>
    updateProduct(
            @PathVariable String id,
            @Valid @RequestBody ProductRequest request) {

        try {

            logger.info(
                    "Updating product: {}",
                    id
            );

            ProductResponse product =
                    productService.updateProduct(
                            id,
                            request
                    );

            if (product == null) {

                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(
                                new ApiResponse<>(
                                        false,
                                        "Product not found",
                                        null
                                )
                        );
            }

            return ResponseEntity.ok(
                    new ApiResponse<>(
                            true,
                            "Product updated successfully",
                            product
                    )
            );

        } catch (IllegalArgumentException e) {

            logger.warn(
                    "Invalid product data: {}",
                    e.getMessage()
            );

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(
                            new ApiResponse<>(
                                    false,
                                    e.getMessage(),
                                    null
                            )
                    );

        } catch (Exception e) {

            logger.error(
                    "Error updating product: {}",
                    e.getMessage()
            );

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(
                            new ApiResponse<>(
                                    false,
                                    "Error updating product",
                                    null
                            )
                    );
        }
    }

    /**
     * ADMIN:
     * Deactivate product.
     */
    @DeleteMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>>
    deleteProduct(
            @PathVariable String id) {

        try {

            logger.info(
                    "Deactivating product: {}",
                    id
            );

            boolean deleted =
                    productService.deleteProduct(id);

            if (!deleted) {

                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(
                                new ApiResponse<>(
                                        false,
                                        "Product not found",
                                        null
                                )
                        );
            }

            return ResponseEntity.ok(
                    new ApiResponse<>(
                            true,
                            "Product deactivated successfully",
                            null
                    )
            );

        } catch (Exception e) {

            logger.error(
                    "Error deactivating product: {}",
                    e.getMessage()
            );

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(
                            new ApiResponse<>(
                                    false,
                                    "Error deactivating product",
                                    null
                            )
                    );
        }
    }

    /**
     * ADMIN:
     * Upload product image.
     */
    @PostMapping("/admin/{id}/image")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductImageUploadResponse>>
    uploadProductImage(
            @PathVariable String id,
            @RequestParam("image") MultipartFile imageFile) {

        try {

            logger.info(
                    "Uploading image for product: {}",
                    id
            );

            imageCompressionUtil.validateImage(
                    imageFile
            );

            String imageUrl =
                    productService.uploadProductImage(
                            id,
                            imageFile
                    );

            ProductImageUploadResponse response =
                    new ProductImageUploadResponse();

            response.setProductId(id);
            response.setImageUrl(imageUrl);
            response.setMessage(
                    "Image uploaded successfully"
            );

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(
                            new ApiResponse<>(
                                    true,
                                    "Image uploaded successfully",
                                    response
                            )
                    );

        } catch (IllegalArgumentException e) {

            logger.warn(
                    "Invalid image: {}",
                    e.getMessage()
            );

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(
                            new ApiResponse<>(
                                    false,
                                    e.getMessage(),
                                    null
                            )
                    );

        } catch (IOException e) {

            logger.error(
                    "IO error uploading image: {}",
                    e.getMessage()
            );

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(
                            new ApiResponse<>(
                                    false,
                                    "Error processing image",
                                    null
                            )
                    );

        } catch (Exception e) {

            logger.error(
                    "Error uploading image: {}",
                    e.getMessage()
            );

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(
                            new ApiResponse<>(
                                    false,
                                    "Error uploading image",
                                    null
                            )
                    );
        }
    }
}