package com.fixit.dto; import lombok.*; import java.time.LocalDateTime;
@Data @NoArgsConstructor @AllArgsConstructor @Builder public class SecondHandImageResponse { private String id,storagePath,imageUrl; private int imageOrder; private boolean isThumbnail; private LocalDateTime createdAt; }
