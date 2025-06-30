package com.fastcampus.ecommerce.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductReindex implements Serializable {
    private Long productId;
    // can be REINDEX/DELETE
    private String action;
}
