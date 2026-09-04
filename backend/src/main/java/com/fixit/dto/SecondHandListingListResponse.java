package com.fixit.dto; import lombok.*; import java.util.List;
@Data @NoArgsConstructor @AllArgsConstructor @Builder public class SecondHandListingListResponse { private List<SecondHandListingResponse> listings; private int page,pageSize; private long totalCount; }
