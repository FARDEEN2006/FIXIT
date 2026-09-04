package com.fixit.dto; import lombok.*; import java.util.List;
@Data @NoArgsConstructor @AllArgsConstructor @Builder public class ServiceListResponse { private List<ServiceResponse> services; }
