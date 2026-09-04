package com.fixit.dto; import lombok.*; import java.time.LocalDateTime;
@Data @NoArgsConstructor @AllArgsConstructor @Builder public class ProductEnquiryResponse { private String id,productId,productName,customerName,customerPhone,customerEmail,enquiryStatus,whatsappMessage; private LocalDateTime createdAt; }
