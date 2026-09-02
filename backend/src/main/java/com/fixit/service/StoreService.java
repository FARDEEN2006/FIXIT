package com.fixit.service;

import com.fixit.dto.StoreInformationRequest;
import com.fixit.dto.StoreInformationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Store Service
 * 
 * Handles:
 * - Store information management
 * - Business details (name, contact, hours, location)
 */
@Service
public class StoreService {

    private static final Logger logger = LoggerFactory.getLogger(StoreService.class);

    @Autowired
    private SupabaseService supabaseService;

    private static final String STORE_TABLE = "store_information";

    /**
     * Get store information
     */
    public StoreInformationResponse getStoreInfo() {
        logger.info("Fetching store information");
        
        List<Map<String, Object>> results = supabaseService.queryTable(STORE_TABLE, new HashMap<>());
        
        if (results.isEmpty()) {
            logger.warn("Store information not found");
            return null;
        }

        Map<String, Object> storeData = results.get(0);
        return mapToStoreResponse(storeData);
    }

    /**
     * Update store information
     */
    public StoreInformationResponse updateStoreInfo(StoreInformationRequest request) {
        logger.info("Updating store information");
        
        // Get existing store info
        List<Map<String, Object>> results = supabaseService.queryTable(STORE_TABLE, new HashMap<>());
        
        String storeId;
        if (results.isEmpty()) {
            // Create new store info
            Map<String, Object> storeData = new HashMap<>();
            storeData.put("business_name", request.getBusinessName());
            storeData.put("phone", request.getPhone());
            storeData.put("whatsapp", request.getWhatsapp());
            storeData.put("email", request.getEmail());
            storeData.put("address", request.getAddress());
            storeData.put("city", request.getCity());
            storeData.put("state", request.getState());
            storeData.put("pincode", request.getPincode());
            storeData.put("working_hours_open", request.getWorkingHoursOpen());
            storeData.put("working_hours_close", request.getWorkingHoursClose());
            storeData.put("about_content", request.getAboutContent());
            storeData.put("map_lat", request.getMapLat());
            storeData.put("map_lon", request.getMapLon());

            Map<String, Object> result = supabaseService.insertRecord(STORE_TABLE, storeData);
            
            if (result != null) {
                logger.info("Store information created");
                return mapToStoreResponse(result);
            }
            throw new RuntimeException("Failed to create store information");
        } else {
            storeId = (String) results.get(0).get("id");
            
            Map<String, Object> updateData = new HashMap<>();
            updateData.put("business_name", request.getBusinessName());
            updateData.put("phone", request.getPhone());
            updateData.put("whatsapp", request.getWhatsapp());
            updateData.put("email", request.getEmail());
            updateData.put("address", request.getAddress());
            updateData.put("city", request.getCity());
            updateData.put("state", request.getState());
            updateData.put("pincode", request.getPincode());
            updateData.put("working_hours_open", request.getWorkingHoursOpen());
            updateData.put("working_hours_close", request.getWorkingHoursClose());
            updateData.put("about_content", request.getAboutContent());
            updateData.put("map_lat", request.getMapLat());
            updateData.put("map_lon", request.getMapLon());

            if (supabaseService.updateRecord(STORE_TABLE, storeId, updateData)) {
                logger.info("Store information updated");
                return getStoreInfo();
            }
            throw new RuntimeException("Failed to update store information");
        }
    }

    /**
     * Map database row to StoreInformationResponse
     */
    private StoreInformationResponse mapToStoreResponse(Map<String, Object> row) {
        return StoreInformationResponse.builder()
                .id((String) row.get("id"))
                .businessName((String) row.get("business_name"))
                .phone((String) row.get("phone"))
                .whatsapp((String) row.get("whatsapp"))
                .email((String) row.get("email"))
                .address((String) row.get("address"))
                .city((String) row.get("city"))
                .state((String) row.get("state"))
                .pincode((String) row.get("pincode"))
                .workingHoursOpen((String) row.get("working_hours_open"))
                .workingHoursClose((String) row.get("working_hours_close"))
                .aboutContent((String) row.get("about_content"))
                .mapLat((Double) row.get("map_lat"))
                .mapLon((Double) row.get("map_lon"))
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
