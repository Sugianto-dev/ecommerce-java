package com.fastcampus.ecommerce.service;

import com.fastcampus.ecommerce.model.ProductReindex;

public interface ProductReindexProducer {
    void publishProductReindex(ProductReindex message);
}
