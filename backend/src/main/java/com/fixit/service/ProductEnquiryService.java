package com.fixit.service;

import com.fixit.dto.ProductEnquiryRequest;
import com.fixit.dto.ProductEnquiryResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Product Enquiry Service
 * 
 * Handles:
 * - Customer product enquiries
 * - WhatsApp message generation
 * - Admin enquiry list retrieval
 * - Enquiry status tracking
 */
@Service
public class ProductEnquiryService {

    private static final Logger logger = LoggerFactory.getLogger(ProductEnquiryService.class);

    @Autowired
    private SupabaseService supabaseService;

    @Value("${app.whatsapp-number}")
    private String businessWhatsappNumber;

    private static final String ENQUIRIES_TABLE = "product_enquiries";
    private static final String PRODUCTS_TABLE = "products";

    /**
     * Create product enquiry
     * Generates WhatsApp message and notifies admin
     */
    public ProductEnquiryResponse createEnquiry(ProductEnquiryRequest request) {
        logger.info("Creating product enquiry for product: {}, customer: {}", request.getProductId(), request.getCustomerName());
        
        // Verify product exists
        if (!productExists(request.getProductId())) {
            throw new IllegalArgumentException("Product not found: " + request.getProductId());
        }

        // Generate WhatsApp message
        String whatsappMessage = generateWhatsappMessage(request);

        Map<String, Object> enquiryData = new HashMap<>();
        enquiryData.put("product_id", request.getProductId());
        enquiryData.put("customer_name", request.getCustomerName());
        enquiryData.put("customer_phone", request.getCustomerPhone());
        enquiryData.put("customer_email", request.getCustomerEmail());
        enquiryData.put("enquiry_status", "PENDING");

        Map<String, Object> result = supabaseService.insertRecord(ENQUIRIES_TABLE, enquiryData);
        
        if (result != null) {
            logger.info("Enquiry created successfully: {}", result.get("id"));
            
            ProductEnquiryResponse response = mapToEnquiryResponse(result);
            response.setWhatsappMessage(whatsappMessage);
            return response;
        }

        throw new RuntimeException("Failed to create enquiry");
    }

    /**
     * Get all enquiries for a product (Admin only)
     */
    public List<ProductEnquiryResponse> getProductEnquiries(String productId, int page, int pageSize) {
        logger.info("Fetching enquiries for product: {} - page: {}", productId, page);
        
        Map<String, String> filters = new HashMap<>();
        filters.put("product_id", "eq." + productId);
        filters.put("order", "created_at.desc");
        filters.put("limit", String.valueOf(pageSize));
        filters.put("offset", String.valueOf(page * pageSize));

        List<Map<String, Object>> results = supabaseService.queryTable(ENQUIRIES_TABLE, filters);
        List<ProductEnquiryResponse> enquiries = new ArrayList<>();
        
        for (Map<String, Object> row : results) {
            enquiries.add(mapToEnquiryResponse(row));
        }

        return enquiries;
    }

    /**
     * Get all enquiries (Admin only - all products)
     */
    public List<ProductEnquiryResponse> getAllEnquiries(String status, int page, int pageSize) {
        logger.info("Fetching all enquiries - status: {}, page: {}", status, page);
        
        Map<String, String> filters = new HashMap<>();
        if (status != null && !status.isEmpty()) {
            filters.put("enquiry_status", "eq." + status);
        }
        filters.put("order", "created_at.desc");
        filters.put("limit", String.valueOf(pageSize));
        filters.put("offset", String.valueOf(page * pageSize));

        List<Map<String, Object>> results = supabaseService.queryTable(ENQUIRIES_TABLE, filters);
        List<ProductEnquiryResponse> enquiries = new ArrayList<>();
        
        for (Map<String, Object> row : results) {
            enquiries.add(mapToEnquiryResponse(row));
        }

        return enquiries;
    }

    /**
     * Get enquiry by ID
     */
    public ProductEnquiryResponse getEnquiryById(String enquiryId) {
        logger.info("Fetching enquiry: {}", enquiryId);
        
        Map<String, String> filters = new HashMap<>();
        filters.put("id", "eq." + enquiryId);

        List<Map<String, Object>> results = supabaseService.queryTable(ENQUIRIES_TABLE, filters);
        
        if (results.isEmpty()) {
            logger.warn("Enquiry not found: {}", enquiryId);
            return null;
        }

        return mapToEnquiryResponse(results.get(0));
    }

    /**
     * Update enquiry status
     */
    public boolean updateEnquiryStatus(String enquiryId, String status) {
        logger.info("Updating enquiry status - ID: {}, new status: {}", enquiryId, status);
        
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("enquiry_status", status);

        return supabaseService.updateRecord(ENQUIRIES_TABLE, enquiryId, updateData);
    }

    /**
     * Delete enquiry
     */
    public boolean deleteEnquiry(String enquiryId) {
        logger.info("Deleting enquiry: {}", enquiryId);
        return supabaseService.deleteRecord(ENQUIRIES_TABLE, enquiryId);
    }

    /**
     * Generate WhatsApp message template
     */
    private String generateWhatsappMessage(ProductEnquiryRequest request) {
        return "Hi, I am interested in the product. " +
               "My name is " + request.getCustomerName() + 
               " and my phone number is " + request.getCustomerPhone() +
               ". Please contact me at your earliest convenience. " +
               "Email: " + request.getCustomerEmail();
    }

    /**
     * Check if product exists
     */
    private boolean productExists(String productId) {
        Map<String, String> filters = new HashMap<>();
        filters.put("id", "eq." + productId);
        
        List<Map<String, Object>> results = supabaseService.queryTable(PRODUCTS_TABLE, filters);
        return !results.isEmpty();
    }

    /**
     * Get product name
     */
    private String getProductName(String productId) {
        Map<String, String> filters = new HashMap<>();
        filters.put("id", "eq." + productId);
        
        List<Map<String, Object>> results = supabaseService.queryTable(PRODUCTS_TABLE, filters);
        if (!results.isEmpty()) {
            return (String) results.get(0).get("name");
        }
        return "Unknown Product";
    }

    /**
     * Map database row to ProductEnquiryResponse
     */
    private ProductEnquiryResponse mapToEnquiryResponse(Map<String, Object> row) {
        return ProductEnquiryResponse.builder()
                .id((String) row.get("id"))
                .productId((String) row.get("product_id"))
                .customerName((String) row.get("customer_name"))
                .customerPhone((String) row.get("customer_phone"))
                .customerEmail((String) row.get("customer_email"))
                .whatsappMessage((String) row.get("whatsapp_message"))
                .enquiryStatus((String) row.get("enquiry_status"))
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
