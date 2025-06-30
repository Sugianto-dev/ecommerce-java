package com.fastcampus.ecommerce.service;

import com.fastcampus.ecommerce.model.UserActivityReindex;

public interface UserActivityReindexProducer {
    void publishUserActivityReindex(UserActivityReindex message);
}
