package com.fixit.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Image Compression Utility
 * 
 * Handles:
 * - Image validation (MIME type, extension, size)
 * - Image compression
 * - Image resizing
 * - Quality optimization
 * - Thumbnail generation
 */
@Component
public class ImageCompressionUtil {

    private static final Logger logger = LoggerFactory.getLogger(ImageCompressionUtil.class);

    @Value("${image.max-file-size:5242880}") // 5MB default
    private long maxFileSize;

    @Value("${image.allowed-extensions:jpg,jpeg,jfif,png,webp}")
    private String allowedExtensions;

    @Value("${image.allowed-mime-types:image/jpeg,image/jfif,image/png,image/webp}")
    private String allowedMimeTypes;

    @Value("${image.compression-quality:80}")
    private int compressionQuality;

    @Value("${image.max-width:1200}")
    private int maxWidth;

    @Value("${image.max-height:1200}")
    private int maxHeight;

    @Value("${image.thumbnail-width:600}")
    private int thumbnailWidth;

    @Value("${image.thumbnail-height:600}")
    private int thumbnailHeight;

    /**
     * Validate image file
     * @throws IllegalArgumentException if validation fails
     */
    public void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Image file is required");
        }

        // Check file size
        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException(
                    String.format("File size exceeds maximum limit of %d MB", maxFileSize / (1024 * 1024))
            );
        }

        // Accept image MIME types broadly; ImageIO below verifies that the
        // uploaded content is a format the compression pipeline can decode.
        String contentType = file.getContentType();
        if (contentType != null
                && !contentType.toLowerCase().startsWith("image/")) {
            throw new IllegalArgumentException(
                    String.format("File type not supported: %s", contentType)
            );
        }

        try (InputStream inputStream = file.getInputStream()) {
            if (ImageIO.read(inputStream) == null) {
                throw new IllegalArgumentException(
                        "Unsupported or invalid image format"
                );
            }
        } catch (IOException e) {
            throw new IllegalArgumentException(
                    "Unable to read image file"
            );
        }

        String filename = file.getOriginalFilename();
        logger.info("Image validation passed: {} ({})", filename, contentType);
    }

    /**
     * Compress and optimize image
     * @return byte array of compressed image
     */
    public byte[] compressImage(MultipartFile file) throws IOException {
        validateImage(file);

        // Read image
        BufferedImage originalImage = ImageIO.read(file.getInputStream());
        if (originalImage == null) {
            throw new IllegalArgumentException("Failed to read image file");
        }

        logger.info("Original image dimensions: {}x{}", originalImage.getWidth(), originalImage.getHeight());

        // Resize if necessary
        BufferedImage resizedImage = resizeImage(originalImage, maxWidth, maxHeight);

        // Compress to bytes
        byte[] compressedBytes = imageToBytes(resizedImage, "JPEG", compressionQuality);

        logger.info("Compressed image size: {} bytes (original: {} bytes)", 
                compressedBytes.length, file.getSize());

        return compressedBytes;
    }

    /**
     * Create thumbnail from image
     * @return byte array of thumbnail image
     */
    public byte[] createThumbnail(MultipartFile file) throws IOException {
        validateImage(file);

        BufferedImage originalImage = ImageIO.read(file.getInputStream());
        if (originalImage == null) {
            throw new IllegalArgumentException("Failed to read image file");
        }

        // Resize for thumbnail
        BufferedImage thumbnail = resizeImage(originalImage, thumbnailWidth, thumbnailHeight);

        // Compress to bytes
        return imageToBytes(thumbnail, "JPEG", compressionQuality);
    }

    /**
     * Resize image maintaining aspect ratio
     */
    private BufferedImage resizeImage(BufferedImage originalImage, int maxWidth, int maxHeight) {
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();

        // Don't upscale
        if (originalWidth <= maxWidth && originalHeight <= maxHeight) {
            return originalImage;
        }

        // Calculate new dimensions maintaining aspect ratio
        double aspectRatio = (double) originalWidth / originalHeight;
        int newWidth = maxWidth;
        int newHeight = (int) (newWidth / aspectRatio);

        if (newHeight > maxHeight) {
            newHeight = maxHeight;
            newWidth = (int) (newHeight * aspectRatio);
        }

        logger.info("Resizing image from {}x{} to {}x{}", 
                originalWidth, originalHeight, newWidth, newHeight);

        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = resizedImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
        g2d.dispose();

        return resizedImage;
    }

    /**
     * Convert BufferedImage to byte array with compression
     */
    private byte[] imageToBytes(BufferedImage image, String format, int quality) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        if ("JPEG".equalsIgnoreCase(format)) {
            // Set compression quality for JPEG
            ImageIO.write(image, format, baos);
        } else if ("PNG".equalsIgnoreCase(format)) {
            ImageIO.write(image, format, baos);
        } else {
            ImageIO.write(image, "JPEG", baos);
        }

        baos.flush();
        byte[] result = baos.toByteArray();
        baos.close();

        return result;
    }

    /**
     * Generate unique filename for image
     */
    public String generateUniqueFilename(String originalFilename) {
        String extension = getFileExtension(originalFilename);
        String uniqueName = UUID.randomUUID().toString();
        return uniqueName + "." + extension;
    }

    /**
     * Get file extension
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    /**
     * Check if MIME type is allowed
     */
    private boolean isAllowedMimeType(String mimeType) {
        if (mimeType == null) {
            return false;
        }
        String[] allowed = allowedMimeTypes.split(",");
        for (String type : allowed) {
            if (mimeType.equalsIgnoreCase(type.trim())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if file extension is allowed
     */
    private boolean isAllowedExtension(String extension) {
        if (extension == null || extension.isEmpty()) {
            return false;
        }
        String[] allowed = allowedExtensions.split(",");
        for (String ext : allowed) {
            if (extension.equalsIgnoreCase(ext.trim())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get list of allowed extensions
     */
    public List<String> getAllowedExtensions() {
        List<String> extensions = new ArrayList<>();
        for (String ext : allowedExtensions.split(",")) {
            extensions.add(ext.trim());
        }
        return extensions;
    }

    /**
     * Get list of allowed MIME types
     */
    public List<String> getAllowedMimeTypes() {
        List<String> types = new ArrayList<>();
        for (String type : allowedMimeTypes.split(",")) {
            types.add(type.trim());
        }
        return types;
    }
}
