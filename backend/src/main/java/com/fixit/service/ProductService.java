package com.fixit.service;

import com.fixit.dto.ProductRequest;
import com.fixit.dto.ProductResponse;
import com.fixit.dto.ProductDetailResponse;
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
 * Product Service
 * 
 * Handles:
 * - Product CRUD operations
 * - Product image upload and management
 * - Product listing retrieval
 */
@Service
public class ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    @Autowired
    private SupabaseService supabaseService;

    @Autowired
    private ImageCompressionUtil imageCompressionUtil;

    @Value("${image.max-product-images:1}")
    private int maxProductImages;

    private static final String PRODUCTS_TABLE = "products";
    private static final String PRODUCTS_BUCKET = "products";

    /**
     * Get all active products
     */
    public List<ProductResponse> getAllActiveProducts(int page, int pageSize) {
        logger.info("Fetching active products - page: {}, size: {}", page, pageSize);
        
        // Query: SELECT * FROM products WHERE is_active = true ORDER BY created_at DESC LIMIT pageSize OFFSET page*pageSize
        Map<String, String> filters = new HashMap<>();
        filters.put("is_active", "eq.true");
        filters.put("order", "created_at.desc");
        filters.put("limit", String.valueOf(pageSize));
        filters.put("offset", String.valueOf(page * pageSize));

        List<Map<String, Object>> results = supabaseService.queryTable(PRODUCTS_TABLE, filters);
        
        List<ProductResponse> products = new ArrayList<>();
        for (Map<String, Object> row : results) {
            products.add(mapToProductResponse(row));
        }

        return products;
    }

    /**
     * Get product by ID with details
     */
    public ProductDetailResponse getProductById(String productId) {
        logger.info("Fetching product details for: {}", productId);
        
        Map<String, String> filters = new HashMap<>();
        filters.put("id", "eq." + productId);

        List<Map<String, Object>> results = supabaseService.queryTable(PRODUCTS_TABLE, filters);
        
        if (results.isEmpty()) {
            logger.warn("Product not found: {}", productId);
            return null;
        }

        Map<String, Object> product = results.get(0);
        ProductDetailResponse response = mapToProductDetailResponse(product);
        
        // Get enquiry count
        response.setEnquiryCount(getProductEnquiryCount(productId));
        
        // Generate image URL
        String imagePath = (String) product.get("image_path");
        if (imagePath != null && !imagePath.isEmpty()) {
            response.setImageUrl(supabaseService.generatePublicUrl(PRODUCTS_BUCKET, imagePath));
        }

        return response;
    }

    /**
     * Create new product (Admin only)
     */
    public ProductResponse createProduct(ProductRequest request) {
        logger.info("Creating new product: {}", request.getName());
        
        Map<String, Object> productData = new HashMap<>();
        productData.put("name", request.getName());
        productData.put("price", request.getPrice());
        productData.put("description", request.getDescription());
        productData.put("is_active", request.isActive());

        Map<String, Object> result = supabaseService.insertRecord(PRODUCTS_TABLE, productData);
        
        if (result != null) {
            logger.info("Product created successfully: {}", result.get("id"));
            return mapToProductResponse(result);
        }
        
        logger.error("Failed to create product");
        return null;
    }

    /**
     * Upload product image
     */
    public String uploadProductImage(String productId, MultipartFile imageFile) throws IOException {
        logger.info("Uploading image for product: {}", productId);
        
        // Validate product exists
        ProductResponse product = getProductById(productId);
        if (product == null) {
            throw new IllegalArgumentException("Product not found: " + productId);
        }

        // Delete existing image if any
        if (product.getImagePath() != null && !product.getImagePath().isEmpty()) {
            supabaseService.deleteFile(PRODUCTS_BUCKET, product.getImagePath());
        }

        // Compress image
        byte[] compressedImage = imageCompressionUtil.compressImage(imageFile);
        
        // Generate unique filename
        String filename = imageCompressionUtil.generateUniqueFilename(imageFile.getOriginalFilename());
        String filePath = productId + "/" + filename;

        // Upload to storage
        String uploadedPath = supabaseService.uploadFile(PRODUCTS_BUCKET, filePath, compressedImage);
        
        if (uploadedPath != null) {
            // Update product record
            Map<String, Object> updateData = new HashMap<>();
            updateData.put("image_path", uploadedPath);
            
            if (supabaseService.updateRecord(PRODUCTS_TABLE, productId, updateData)) {
                logger.info("Product image uploaded and record updated: {}", filePath);
                return supabaseService.generatePublicUrl(PRODUCTS_BUCKET, uploadedPath);
            }
        }

        throw new RuntimeException("Failed to upload product image");
    }

    /**
     * Update product
     */
    public ProductResponse updateProduct(String productId, ProductRequest request) {
        logger.info("Updating product: {}", productId);
        
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("name", request.getName());
        updateData.put("price", request.getPrice());
        updateData.put("description", request.getDescription());
        updateData.put("is_active", request.isActive());

        if (supabaseService.updateRecord(PRODUCTS_TABLE, productId, updateData)) {
            logger.info("Product updated successfully: {}", productId);
            return getProductById(productId);
        }

        logger.error("Failed to update product: {}", productId);
        return null;
    }

    /**
     * Delete product (soft delete - deactivate)
     */
    public boolean deleteProduct(String productId) {
        logger.info("Deleting product: {}", productId);
        
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("is_active", false);

        return supabaseService.updateRecord(PRODUCTS_TABLE, productId, updateData);
    }

    /**
     * Get product enquiry count
     */
    private long getProductEnquiryCount(String productId) {
        // This would query the product_enquiries table
        // Implementation depends on how you query the database
        // For now, returning 0
        return 0;
    }

    /**
     * Map database row to ProductResponse
     */
    private ProductResponse mapToProductResponse(Map<String, Object> row) {
        return ProductResponse.builder()
                .id((String) row.get("id"))
                .name((String) row.get("name"))
                .price(new java.math.BigDecimal(row.get("price").toString()))
                .description((String) row.get("description"))
                .imagePath((String) row.get("image_path"))
                .isActive((Boolean) row.get("is_active"))
                .createdAt(parseTimestamp((String) row.get("created_at")))
                .updatedAt(parseTimestamp((String) row.get("updated_at")))
                .build();
    }

    /**
     * Map database row to ProductDetailResponse
     */
    private ProductDetailResponse mapToProductDetailResponse(Map<String, Object> row) {
        return ProductDetailResponse.builder()
                .id((String) row.get("id"))
                .name((String) row.get("name"))
                .price(new java.math.BigDecimal(row.get("price").toString()))
                .description((String) row.get("description"))
                .imagePath((String) row.get("image_path"))
                .isActive((Boolean) row.get("is_active"))
                .enquiryCount(0) // Will be set by caller
                .createdAt(parseTimestamp((String) row.get("created_at")))
                .updatedAt(parseTimestamp((String) row.get("updated_at")))
                .build();
    }

    /**
     * Parse timestamp string
     */
    private java.time.LocalDateTime parseTimestamp(String timestamp) {
        if (timestamp == null) return null;
        return java.time.LocalDateTime.parse(timestamp, java.time.format.DateTimeFormatter.ISO_DATE_TIME);
    }
}
