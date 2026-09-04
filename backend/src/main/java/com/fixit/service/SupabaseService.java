package com.fixit.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SupabaseService {

    private static final Logger logger =
            LoggerFactory.getLogger(SupabaseService.class);

    private final RestTemplate restTemplate;

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.service-key}")
    private String serviceKey;

    public SupabaseService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Common headers for Supabase service-role requests.
     */
    private HttpHeaders createHeaders() {

        HttpHeaders headers = new HttpHeaders();

        headers.set(
                "apikey",
                serviceKey
        );

        headers.set(
                "Authorization",
                "Bearer " + serviceKey
        );

        headers.setContentType(
                MediaType.APPLICATION_JSON
        );

        return headers;
    }

    /**
     * Call Supabase RPC function.
     */
    public Map<String, Object> callRpcFunction(
            String functionName,
            Map<String, Object> params) {

        try {

            String url =
                    supabaseUrl
                            + "/rest/v1/rpc/"
                            + functionName;

            HttpHeaders headers = createHeaders();

            headers.set(
                    "Prefer",
                    "return=representation"
            );

            HttpEntity<Map<String, Object>> request =
                    new HttpEntity<>(
                            params,
                            headers
                    );

            ResponseEntity<Map> response =
                    restTemplate.postForEntity(
                            url,
                            request,
                            Map.class
                    );

            if (response.getStatusCode().is2xxSuccessful()) {

                logger.info(
                        "RPC function {} called successfully",
                        functionName
                );

                return response.getBody();
            }

            logger.error(
                    "RPC function {} failed with status {}",
                    functionName,
                    response.getStatusCode()
            );

            return null;

        } catch (Exception e) {

            logger.error(
                    "Error calling RPC function {}: {}",
                    functionName,
                    e.getMessage(),
                    e
            );

            return null;
        }
    }

    /**
     * Query Supabase table.
     */
    public List<Map<String, Object>> queryTable(
            String tableName,
            Map<String, String> filters) {

        try {

            StringBuilder urlBuilder =
                    new StringBuilder(
                            supabaseUrl
                                    + "/rest/v1/"
                                    + tableName
                    );

            if (filters != null && !filters.isEmpty()) {

                urlBuilder.append("?");

                filters.forEach((key, value) -> {

                    urlBuilder
                            .append(key)
                            .append("=")
                            .append(value)
                            .append("&");
                });

                urlBuilder.setLength(
                        urlBuilder.length() - 1
                );
            }

            HttpHeaders headers = createHeaders();

            headers.set(
                    "Accept",
                    MediaType.APPLICATION_JSON_VALUE
            );

            HttpEntity<Void> request =
                    new HttpEntity<>(headers);

            ResponseEntity<List> response =
                    restTemplate.exchange(
                            urlBuilder.toString(),
                            HttpMethod.GET,
                            request,
                            List.class
                    );

            if (response.getStatusCode().is2xxSuccessful()) {

                logger.info(
                        "Query table {} successful",
                        tableName
                );

                return response.getBody() != null
                        ? response.getBody()
                        : new ArrayList<>();
            }

            logger.error(
                    "Query table {} failed with status {}",
                    tableName,
                    response.getStatusCode()
            );

            return new ArrayList<>();

        } catch (Exception e) {

            logger.error(
                    "Error querying table {}: {}",
                    tableName,
                    e.getMessage(),
                    e
            );

            return new ArrayList<>();
        }
    }

    /**
     * Insert record into Supabase table.
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> insertRecord(
            String tableName,
            Map<String, Object> data) {

        try {

            String url =
                    supabaseUrl
                            + "/rest/v1/"
                            + tableName;

            HttpHeaders headers = createHeaders();

            headers.set(
                    "Prefer",
                    "return=representation"
            );

            HttpEntity<Map<String, Object>> request =
                    new HttpEntity<>(
                            data,
                            headers
                    );

            ResponseEntity<List> response =
                    restTemplate.postForEntity(
                            url,
                            request,
                            List.class
                    );

            if (response.getStatusCode().is2xxSuccessful()
                    && response.getBody() != null
                    && !response.getBody().isEmpty()) {

                logger.info(
                        "Record inserted into table {}",
                        tableName
                );

                return (Map<String, Object>)
                        response.getBody().get(0);
            }

            logger.error(
                    "Insert into table {} failed with status {}",
                    tableName,
                    response.getStatusCode()
            );

            return null;

        } catch (Exception e) {

            logger.error(
                    "Error inserting record into table {}: {}",
                    tableName,
                    e.getMessage(),
                    e
            );

            return null;
        }
    }

    /**
     * Update record in Supabase table.
     */
    public boolean updateRecord(
            String tableName,
            String id,
            Map<String, Object> data) {

        try {

            String url =
                    supabaseUrl
                            + "/rest/v1/"
                            + tableName
                            + "?id=eq."
                            + id;

            HttpHeaders headers = createHeaders();

            headers.set(
                    "Prefer",
                    "return=minimal"
            );

            HttpEntity<Map<String, Object>> request =
                    new HttpEntity<>(
                            data,
                            headers
                    );

            ResponseEntity<Void> response =
                    restTemplate.exchange(
                            url,
                            HttpMethod.PATCH,
                            request,
                            Void.class
                    );

            if (response.getStatusCode().is2xxSuccessful()) {

                logger.info(
                        "Record updated in table {}",
                        tableName
                );

                return true;
            }

            logger.error(
                    "Update table {} failed with status {}",
                    tableName,
                    response.getStatusCode()
            );

            return false;

        } catch (Exception e) {

            logger.error(
                    "Error updating record in table {}: {}",
                    tableName,
                    e.getMessage(),
                    e
            );

            return false;
        }
    }

    /**
     * Delete record from Supabase table.
     */
    public boolean deleteRecord(
            String tableName,
            String id) {

        try {

            String url =
                    supabaseUrl
                            + "/rest/v1/"
                            + tableName
                            + "?id=eq."
                            + id;

            HttpHeaders headers = createHeaders();

            HttpEntity<Void> request =
                    new HttpEntity<>(headers);

            ResponseEntity<Void> response =
                    restTemplate.exchange(
                            url,
                            HttpMethod.DELETE,
                            request,
                            Void.class
                    );

            if (response.getStatusCode().is2xxSuccessful()) {

                logger.info(
                        "Record deleted from table {}",
                        tableName
                );

                return true;
            }

            logger.error(
                    "Delete from table {} failed with status {}",
                    tableName,
                    response.getStatusCode()
            );

            return false;

        } catch (Exception e) {

            logger.error(
                    "Error deleting record from table {}: {}",
                    tableName,
                    e.getMessage(),
                    e
            );

            return false;
        }
    }

    /**
     * Upload file to Supabase Storage.
     */
    public String uploadFile(
            String bucketName,
            String filePath,
            byte[] fileContent) {

        return uploadFileWithContentType(
                bucketName,
                filePath,
                fileContent,
                "image/jpeg"
        );
    }

    /**
     * Upload file with the correct MIME type.
     */
    public String uploadFileWithContentType(
            String bucketName,
            String filePath,
            byte[] fileContent,
            String contentType) {

        try {

            String url =
                    supabaseUrl
                            + "/storage/v1/object/"
                            + bucketName
                            + "/"
                            + filePath;

            HttpHeaders headers = createHeaders();

            headers.set(
                    "Content-Type",
                    contentType
            );

            headers.set(
                    "x-upsert",
                    "false"
            );

            HttpEntity<byte[]> request =
                    new HttpEntity<>(
                            fileContent,
                            headers
                    );

            ResponseEntity<Map> response =
                    restTemplate.postForEntity(
                            url,
                            request,
                            Map.class
                    );

            if (response.getStatusCode().is2xxSuccessful()) {

                logger.info(
                        "File uploaded to bucket {}: {}",
                        bucketName,
                        filePath
                );

                return filePath;
            }

            logger.error(
                    "File upload failed with status: {}",
                    response.getStatusCode()
            );

            return null;

        } catch (Exception e) {

            logger.error(
                    "Error uploading file to storage: {}",
                    e.getMessage(),
                    e
            );

            return null;
        }
    }

    /**
     * Generate public URL for a public bucket.
     */
    public String generatePublicUrl(
            String bucketName,
            String filePath) {

        return supabaseUrl
                + "/storage/v1/object/public/"
                + bucketName
                + "/"
                + filePath;
    }

    /**
     * Generate signed URL for a private bucket.
     *
     * Supabase may return:
     *
     * /object/sign/bucket/file.jpg?token=...
     *
     * or:
     *
     * /storage/v1/object/sign/bucket/file.jpg?token=...
     *
     * or an already complete HTTPS URL.
     *
     * This method converts all supported forms into a complete
     * Supabase Storage URL for the browser.
     */
    public String generateSignedUrl(
            String bucketName,
            String filePath,
            long expirationSeconds) {

        try {

            String url =
                    supabaseUrl
                            + "/storage/v1/object/sign/"
                            + bucketName
                            + "/"
                            + filePath;

            Map<String, Object> requestBody =
                    new HashMap<>();

            requestBody.put(
                    "expiresIn",
                    expirationSeconds
            );

            HttpHeaders headers = createHeaders();

            HttpEntity<Map<String, Object>> request =
                    new HttpEntity<>(
                            requestBody,
                            headers
                    );

            ResponseEntity<Map> response =
                    restTemplate.postForEntity(
                            url,
                            request,
                            Map.class
                    );

            if (response.getStatusCode().is2xxSuccessful()
                    && response.getBody() != null) {

                String signedPath =
                        (String) response.getBody()
                                .get("signedURL");

                if (signedPath == null
                        || signedPath.isEmpty()) {

                    logger.error(
                            "Supabase returned an empty signed URL for: {}",
                            filePath
                    );

                    return null;
                }

                String signedUrl;

                /*
                 * Case 1:
                 * Supabase already returned a complete URL.
                 */
                if (signedPath.startsWith("http://")
                        || signedPath.startsWith("https://")) {

                    signedUrl = signedPath;

                /*
                 * Case 2:
                 * Supabase returned:
                 * /storage/v1/object/sign/...
                 */
                } else if (
                        signedPath.startsWith(
                                "/storage/v1/"
                        )) {

                    signedUrl =
                            supabaseUrl
                                    + signedPath;

                /*
                 * Case 3:
                 * Supabase returned:
                 * /object/sign/...
                 *
                 * Add the missing /storage/v1 part.
                 */
                } else if (
                        signedPath.startsWith(
                                "/object/sign/"
                        )) {

                    signedUrl =
                            supabaseUrl
                                    + "/storage/v1"
                                    + signedPath;

                /*
                 * Case 4:
                 * Supabase returned:
                 * object/sign/...
                 */
                } else if (
                        signedPath.startsWith(
                                "object/sign/"
                        )) {

                    signedUrl =
                            supabaseUrl
                                    + "/storage/v1/"
                                    + signedPath;

                /*
                 * Fallback:
                 * Build the expected Storage signed URL.
                 */
                } else {

                    signedUrl =
                            supabaseUrl
                                    + "/storage/v1/"
                                    + signedPath;
                }

                logger.info(
                        "Signed URL generated successfully for file: {}",
                        filePath
                );

                logger.debug(
                        "Final signed URL: {}",
                        signedUrl
                );

                return signedUrl;
            }

            logger.error(
                    "Failed to generate signed URL for: {}",
                    filePath
            );

            return null;

        } catch (Exception e) {

            logger.error(
                    "Error generating signed URL: {}",
                    e.getMessage(),
                    e
            );

            return null;
        }
    }

    /**
     * Delete file from Supabase Storage.
     */
    public boolean deleteFile(
            String bucketName,
            String filePath) {

        try {

            String url =
                    supabaseUrl
                            + "/storage/v1/object/"
                            + bucketName
                            + "/"
                            + filePath;

            HttpHeaders headers = createHeaders();

            HttpEntity<Void> request =
                    new HttpEntity<>(headers);

            ResponseEntity<Void> response =
                    restTemplate.exchange(
                            url,
                            HttpMethod.DELETE,
                            request,
                            Void.class
                    );

            if (response.getStatusCode().is2xxSuccessful()) {

                logger.info(
                        "File deleted from bucket {}: {}",
                        bucketName,
                        filePath
                );

                return true;
            }

            logger.error(
                    "File deletion failed with status: {}",
                    response.getStatusCode()
            );

            return false;

        } catch (Exception e) {

            logger.error(
                    "Error deleting file from storage: {}",
                    e.getMessage(),
                    e
            );

            return false;
        }
    }
}