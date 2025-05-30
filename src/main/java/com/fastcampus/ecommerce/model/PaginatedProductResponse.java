package com.fastcampus.ecommerce.model;

import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Data;

import java.util.List;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;

@Data
@Builder
@JsonNaming(SnakeCaseStrategy.class)
public class PaginatedProductResponse {
    private List<ProductResponse> data;
    private int pageNo;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean last;
}
