package com.fixit.dto; import lombok.*; import java.time.LocalDateTime;
@Data @NoArgsConstructor @AllArgsConstructor @Builder public class ServiceResponse { private String id,serviceName,description,iconPath,iconUrl; private boolean isActive; private int displayOrder; private LocalDateTime createdAt,updatedAt; }
