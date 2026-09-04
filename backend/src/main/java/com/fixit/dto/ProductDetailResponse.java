package com.fixit.dto; import lombok.*; import java.math.BigDecimal; import java.time.LocalDateTime;
@Data @NoArgsConstructor @AllArgsConstructor @Builder public class ProductDetailResponse { private String id,name,description,imagePath,imageUrl; private BigDecimal price; private boolean isActive; private long enquiryCount; private LocalDateTime createdAt,updatedAt; }
