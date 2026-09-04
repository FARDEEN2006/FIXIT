package com.fixit.service;

import com.fixit.dto.SecondHandListingRequest;
import com.fixit.dto.SecondHandListingResponse;
import com.fixit.dto.SecondHandImageResponse;
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
 * Second-Hand Listing Service
 *
 * Handles:
 * - Second-hand listing CRUD
 * - Image upload and management (max 4 images)
 * - Listing status tracking
 * - Admin operations
 *
 * NOTE: Listings are PRIVATE and only accessible to authenticated admin users.
 */
@Service
public class SecondHandListingService {

    private static final Logger logger =
            LoggerFactory.getLogger(
                    SecondHandListingService.class
            );

    @Autowired
    private SupabaseService supabaseService;

    @Autowired
    private ImageCompressionUtil imageCompressionUtil;

    @Value("${image.max-secondhand-images:4}")
    private int maxImages;

    @Value("${app.whatsapp-number}")
    private String whatsappNumber;

    private static final String LISTINGS_TABLE =
            "second_hand_listings";

    private static final String IMAGES_TABLE =
            "second_hand_images";

    private static final String SECONDHAND_BUCKET =
            "second-hand";

    /**
     * Creates a private listing and stores its
     * validated, optimized images.
     */
    public SecondHandListingResponse createListing(
            SecondHandListingRequest request,
            List<MultipartFile> images
    ) throws IOException {

        logger.info(
                "Creating second-hand listing request for: {}",
                request.getSellerEmail()
        );

        Map<String, Object> listingData =
                new HashMap<>();

        listingData.put(
                "seller_name",
                request.getSellerName()
        );

        listingData.put(
                "seller_phone",
                request.getSellerPhone()
        );

        listingData.put(
                "seller_email",
                request.getSellerEmail() == null
                        ? ""
                        : request.getSellerEmail()
        );

        listingData.put(
                "product_name",
                request.getProductName()
        );

        listingData.put(
                "condition",
                request.getCondition()
        );

        listingData.put(
                "detailed_description",
                request.getDetailedDescription()
        );

        listingData.put(
                "expected_price",
                request.getExpectedPrice()
        );

        listingData.put(
                "listing_status",
                "NEW"
        );

        Map<String, Object> result =
                supabaseService.insertRecord(
                        LISTINGS_TABLE,
                        listingData
                );

        if (result != null) {

            String listingId =
                    (String) result.get("id");

            List<SecondHandImageResponse> storedImages =
                    new ArrayList<>();

            try {

                for (
                        int index = 0;
                        index < images.size();
                        index++
                ) {

                    storedImages.add(
                            addImage(
                                    listingId,
                                    images.get(index),
                                    index == 0
                            )
                    );
                }

            } catch (
                    IOException
                    | RuntimeException failure
            ) {

                deleteListing(listingId);
                throw failure;
            }

            SecondHandListingResponse response =
                    mapToListingResponse(result);

            response.setImages(storedImages);

            return response;
        }

        throw new RuntimeException(
                "Failed to create listing"
        );
    }

    /**
     * Add image to listing (max 4 images).
     */
    public SecondHandImageResponse addImage(
            String listingId,
            MultipartFile imageFile,
            boolean isThumbnail
    ) throws IOException {

        logger.info(
                "Adding image to listing: {} (thumbnail: {})",
                listingId,
                isThumbnail
        );

        Map<String, String> filters =
                new HashMap<>();

        filters.put(
                "listing_id",
                "eq." + listingId
        );

        List<Map<String, Object>> existingImages =
                supabaseService.queryTable(
                        IMAGES_TABLE,
                        filters
                );

        if (existingImages.size() >= maxImages) {

            throw new IllegalArgumentException(
                    "Maximum "
                            + maxImages
                            + " images per listing exceeded"
            );
        }

        byte[] compressedImage =
                imageCompressionUtil.compressImage(
                        imageFile
                );

        String filename =
                imageCompressionUtil.generateUniqueFilename(
                        imageFile.getOriginalFilename()
                );

        String imageOrder =
                String.valueOf(
                        existingImages.size() + 1
                );

        String filePath =
                listingId
                        + "/"
                        + imageOrder
                        + ".jpg";

        String uploadedPath =
                supabaseService.uploadFile(
                        SECONDHAND_BUCKET,
                        filePath,
                        compressedImage
                );

        if (uploadedPath != null) {

            Map<String, Object> imageData =
                    new HashMap<>();

            imageData.put(
                    "listing_id",
                    listingId
            );

            imageData.put(
                    "storage_path",
                    uploadedPath
            );

            imageData.put(
                    "image_order",
                    Integer.parseInt(imageOrder)
            );

            imageData.put(
                    "is_thumbnail",
                    isThumbnail
            );

            Map<String, Object> result =
                    supabaseService.insertRecord(
                            IMAGES_TABLE,
                            imageData
                    );

            if (result != null) {

                logger.info(
                        "Image added to listing: {}",
                        listingId
                );

                return mapToImageResponse(result);
            }
        }

        throw new RuntimeException(
                "Failed to upload image"
        );
    }

    /**
     * Get listing by ID.
     * Admin only.
     *
     * Returns complete listing details
     * including all uploaded images.
     */
    public SecondHandListingResponse getListingById(
            String listingId
    ) {

        logger.info(
                "Fetching listing details: {}",
                listingId
        );

        Map<String, String> filters =
                new HashMap<>();

        filters.put(
                "id",
                "eq." + listingId
        );

        List<Map<String, Object>> results =
                supabaseService.queryTable(
                        LISTINGS_TABLE,
                        filters
                );

        if (results.isEmpty()) {
            return null;
        }

        Map<String, Object> listing =
                results.get(0);

        SecondHandListingResponse response =
                mapToListingResponse(listing);

        Map<String, String> imageFilters =
                new HashMap<>();

        imageFilters.put(
                "listing_id",
                "eq." + listingId
        );

        imageFilters.put(
                "order",
                "image_order.asc"
        );

        List<Map<String, Object>> images =
                supabaseService.queryTable(
                        IMAGES_TABLE,
                        imageFilters
                );

        List<SecondHandImageResponse> imageResponses =
                new ArrayList<>();

        for (Map<String, Object> image : images) {

            SecondHandImageResponse imageResponse =
                    mapToImageResponse(image);

            if (imageResponse != null) {
                imageResponses.add(imageResponse);
            }
        }

        response.setImages(imageResponses);

        return response;
    }

    /**
     * Get all listings.
     * Admin only.
     *
     * The list response now includes the uploaded images
     * so the admin table can display the thumbnail.
     */
    public List<SecondHandListingResponse> getAllListings(
            String status,
            int page,
            int pageSize
    ) {

        logger.info(
                "Fetching all second-hand listings - status: {}, page: {}",
                status,
                page
        );

        Map<String, String> filters =
                new HashMap<>();

        if (status != null && !status.isEmpty()) {

            filters.put(
                    "listing_status",
                    "eq." + status
            );
        }

        filters.put(
                "order",
                "created_at.desc"
        );

        filters.put(
                "limit",
                String.valueOf(pageSize)
        );

        filters.put(
                "offset",
                String.valueOf(
                        page * pageSize
                )
        );

        List<Map<String, Object>> results =
                supabaseService.queryTable(
                        LISTINGS_TABLE,
                        filters
                );

        List<SecondHandListingResponse> listings =
                new ArrayList<>();

        for (Map<String, Object> row : results) {

            SecondHandListingResponse listing =
                    mapToListingResponse(row);

            String listingId =
                    (String) row.get("id");

            /*
             * Fetch images for this listing.
             * The first image is used by the frontend
             * as the thumbnail.
             */
            Map<String, String> imageFilters =
                    new HashMap<>();

            imageFilters.put(
                    "listing_id",
                    "eq." + listingId
            );

            imageFilters.put(
                    "order",
                    "image_order.asc"
            );

            List<Map<String, Object>> images =
                    supabaseService.queryTable(
                            IMAGES_TABLE,
                            imageFilters
                    );

            List<SecondHandImageResponse> imageResponses =
                    new ArrayList<>();

            for (Map<String, Object> image : images) {

                SecondHandImageResponse imageResponse =
                        mapToImageResponse(image);

                if (imageResponse != null) {
                    imageResponses.add(imageResponse);
                }
            }

            /*
             * Maximum 4 images are allowed by the
             * upload logic. Keep that same limit here.
             */
            if (imageResponses.size() > maxImages) {

                imageResponses =
                        new ArrayList<>(
                                imageResponses.subList(
                                        0,
                                        maxImages
                                )
                        );
            }

            listing.setImages(imageResponses);

            listings.add(listing);
        }

        return listings;
    }

    /**
     * Update listing status.
     */
    public boolean updateListingStatus(
            String listingId,
            String newStatus
    ) {

        logger.info(
                "Updating listing status - ID: {}, new status: {}",
                listingId,
                newStatus
        );

        Map<String, Object> updateData =
                new HashMap<>();

        updateData.put(
                "listing_status",
                newStatus
        );

        return supabaseService.updateRecord(
                LISTINGS_TABLE,
                listingId,
                updateData
        );
    }

    /**
     * Delete listing.
     */
    public boolean deleteListing(
            String listingId
    ) {

        logger.info(
                "Deleting listing: {}",
                listingId
        );

        Map<String, String> filters =
                new HashMap<>();

        filters.put(
                "listing_id",
                "eq." + listingId
        );

        List<Map<String, Object>> images =
                supabaseService.queryTable(
                        IMAGES_TABLE,
                        filters
                );

        for (Map<String, Object> image : images) {

            String imagePath =
                    (String) image.get(
                            "storage_path"
                    );

            if (imagePath != null) {

                supabaseService.deleteFile(
                        SECONDHAND_BUCKET,
                        imagePath
                );
            }

            supabaseService.deleteRecord(
                    IMAGES_TABLE,
                    (String) image.get("id")
            );
        }

        return supabaseService.deleteRecord(
                LISTINGS_TABLE,
                listingId
        );
    }

    /**
     * Map database row to listing response.
     */
    private SecondHandListingResponse mapToListingResponse(
            Map<String, Object> row
    ) {

        return SecondHandListingResponse.builder()
                .id(
                        (String) row.get("id")
                )
                .sellerName(
                        (String) row.get("seller_name")
                )
                .sellerPhone(
                        (String) row.get("seller_phone")
                )
                .sellerEmail(
                        (String) row.get("seller_email")
                )
                .productName(
                        (String) row.get("product_name")
                )
                .condition(
                        (String) row.get("condition")
                )
                .detailedDescription(
                        (String) row.get(
                                "detailed_description"
                        )
                )
                .expectedPrice(
                        new java.math.BigDecimal(
                                row.get(
                                        "expected_price"
                                ).toString()
                        )
                )
                .listingStatus(
                        (String) row.get(
                                "listing_status"
                        )
                )
                .emailVerified(
                        (Boolean) row.get(
                                "email_verified"
                        )
                )
                .emailVerifiedAt(
                        parseTimestamp(
                                (String) row.get(
                                        "email_verified_at"
                                )
                        )
                )
                .createdAt(
                        parseTimestamp(
                                (String) row.get(
                                        "created_at"
                                )
                        )
                )
                .updatedAt(
                        parseTimestamp(
                                (String) row.get(
                                        "updated_at"
                                )
                        )
                )
                .build();
    }

    /**
     * Map database row to image response.
     */
    private SecondHandImageResponse mapToImageResponse(
            Map<String, Object> row
    ) {

        String storagePath =
                (String) row.get(
                        "storage_path"
                );

        if (storagePath == null
                || storagePath.isEmpty()) {

            return null;
        }

        String imageUrl =
                supabaseService.generateSignedUrl(
                        SECONDHAND_BUCKET,
                        storagePath,
                        3600
                );

        return SecondHandImageResponse.builder()
                .id(
                        (String) row.get("id")
                )
                .storagePath(
                        storagePath
                )
                .imageUrl(
                        imageUrl
                )
                .imageOrder(
                        (Integer) row.get(
                                "image_order"
                        )
                )
                .isThumbnail(
                        (Boolean) row.get(
                                "is_thumbnail"
                        )
                )
                .createdAt(
                        parseTimestamp(
                                (String) row.get(
                                        "created_at"
                                )
                        )
                )
                .build();
    }

    /**
     * Parse timestamp.
     */
    private java.time.LocalDateTime parseTimestamp(
            String timestamp
    ) {

        if (timestamp == null) {
            return null;
        }

        return java.time.LocalDateTime.parse(
                timestamp,
                java.time.format.DateTimeFormatter.ISO_DATE_TIME
        );
    }
}