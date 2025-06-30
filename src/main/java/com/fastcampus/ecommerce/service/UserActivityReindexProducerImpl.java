package com.fastcampus.ecommerce.service;

import com.fastcampus.ecommerce.model.UserActivityReindex;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserActivityReindexProducerImpl implements UserActivityReindexProducer {
    private final KafkaTemplate<String, UserActivityReindex> kafkaTemplate;
    @Value("${kafka.topic.user-activity-reindex.name}")
    private String topicName;

    @Override
    public void publishUserActivityReindex(UserActivityReindex message) {
        kafkaTemplate.send(topicName, message);
    }
}
