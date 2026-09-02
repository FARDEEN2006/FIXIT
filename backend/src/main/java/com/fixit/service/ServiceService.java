package com.fixit.service;

import com.fixit.dto.ServiceRequest;
import com.fixit.dto.ServiceResponse;
import com.fixit.dto.ServiceListResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service Service
 * 
 * Handles:
 * - Service (repair/maintenance) management
 * - Display order configuration
 * - Icon management
 */
@Service
public class ServiceService {

    private static final Logger logger = LoggerFactory.getLogger(ServiceService.class);

    @Autowired
    private SupabaseService supabaseService;

    private static final String SERVICES_TABLE = "services";

    /**
     * Get all active services
     */
    public ServiceListResponse getAllServices() {
        logger.info("Fetching all active services");
        
        Map<String, String> filters = new HashMap<>();
        filters.put("is_active", "eq.true");
        filters.put("order", "display_order.asc");

        List<Map<String, Object>> results = supabaseService.queryTable(SERVICES_TABLE, filters);
        
        List<ServiceResponse> services = new ArrayList<>();
        for (Map<String, Object> row : results) {
            services.add(mapToServiceResponse(row));
        }

        ServiceListResponse response = new ServiceListResponse();
        response.setServices(services);
        return response;
    }

    /**
     * Get service by ID
     */
    public ServiceResponse getServiceById(String serviceId) {
        logger.info("Fetching service: {}", serviceId);
        
        Map<String, String> filters = new HashMap<>();
        filters.put("id", "eq." + serviceId);

        List<Map<String, Object>> results = supabaseService.queryTable(SERVICES_TABLE, filters);
        
        if (results.isEmpty()) {
            logger.warn("Service not found: {}", serviceId);
            return null;
        }

        return mapToServiceResponse(results.get(0));
    }

    /**
     * Create service
     */
    public ServiceResponse createService(ServiceRequest request) {
        logger.info("Creating service: {}", request.getServiceName());
        
        Map<String, Object> serviceData = new HashMap<>();
        serviceData.put("service_name", request.getServiceName());
        serviceData.put("description", request.getDescription());
        serviceData.put("is_active", request.isActive());
        serviceData.put("display_order", request.getDisplayOrder());

        Map<String, Object> result = supabaseService.insertRecord(SERVICES_TABLE, serviceData);
        
        if (result != null) {
            logger.info("Service created successfully: {}", result.get("id"));
            return mapToServiceResponse(result);
        }

        logger.error("Failed to create service");
        return null;
    }

    /**
     * Update service
     */
    public ServiceResponse updateService(String serviceId, ServiceRequest request) {
        logger.info("Updating service: {}", serviceId);
        
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("service_name", request.getServiceName());
        updateData.put("description", request.getDescription());
        updateData.put("is_active", request.isActive());
        updateData.put("display_order", request.getDisplayOrder());

        if (supabaseService.updateRecord(SERVICES_TABLE, serviceId, updateData)) {
            logger.info("Service updated successfully: {}", serviceId);
            return getServiceById(serviceId);
        }

        logger.error("Failed to update service: {}", serviceId);
        return null;
    }

    /**
     * Delete service
     */
    public boolean deleteService(String serviceId) {
        logger.info("Deleting service: {}", serviceId);
        
        // Soft delete - set is_active to false
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("is_active", false);

        return supabaseService.updateRecord(SERVICES_TABLE, serviceId, updateData);
    }

    /**
     * Map database row to ServiceResponse
     */
    private ServiceResponse mapToServiceResponse(Map<String, Object> row) {
        return ServiceResponse.builder()
                .id((String) row.get("id"))
                .serviceName((String) row.get("service_name"))
                .description((String) row.get("description"))
                .isActive((Boolean) row.get("is_active"))
                .displayOrder((Integer) row.get("display_order"))
                .iconPath((String) row.get("icon_path"))
                .createdAt(parseTimestamp((String) row.get("created_at")))
                .updatedAt(parseTimestamp((String) row.get("updated_at")))
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
