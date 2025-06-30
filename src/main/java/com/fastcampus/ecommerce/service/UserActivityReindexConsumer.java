package com.fastcampus.ecommerce.service;

import com.fastcampus.ecommerce.model.ActivityType;
import com.fastcampus.ecommerce.model.UserActivityReindex;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class UserActivityReindexConsumer {
    private final UserActivityService userActivityService;

    @KafkaListener(
            topics = "${kafka.topic.user-activity-reindex.name}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void listen(UserActivityReindex message) {
        if (!List.of(ActivityType.PURCHASE, ActivityType.VIEW).contains(message.getActivityType())) {
            return;
        }

        if (message.getProductId() == null || message.getUserId() == null) {
            return;
        }

        if (message.getActivityType().equals(ActivityType.PURCHASE)) {
            userActivityService.trackPurchase(message.getProductId(), message.getUserId());
            return;
        }

        if (message.getActivityType().equals(ActivityType.VIEW)) {
            userActivityService.trackProductView(message.getProductId(), message.getUserId());
        }
    }
}
