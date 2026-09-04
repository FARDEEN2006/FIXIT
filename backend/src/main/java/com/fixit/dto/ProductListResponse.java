package com.fixit.dto; import lombok.*; import java.util.List;
@Data @NoArgsConstructor @AllArgsConstructor @Builder public class ProductListResponse { private List<ProductResponse> products; private int page,pageSize; private long totalCount; }
