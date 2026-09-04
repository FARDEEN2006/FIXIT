package com.fixit.dto; import lombok.*; import java.util.List;
@Data @NoArgsConstructor @AllArgsConstructor @Builder public class ProductEnquiryListResponse { private List<ProductEnquiryResponse> enquiries; private int page,pageSize; private long totalCount; }
