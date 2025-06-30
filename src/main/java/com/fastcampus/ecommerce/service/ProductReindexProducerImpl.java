package com.fastcampus.ecommerce.service;

import com.fastcampus.ecommerce.model.ProductReindex;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductReindexProducerImpl implements ProductReindexProducer {
    private final KafkaTemplate<String, ProductReindex> kafkaTemplate;
    @Value("${kafka.topic.product-reindex.name}")
    private String topicName;

    @Override
    public void publishProductReindex(ProductReindex message) {
        kafkaTemplate.send(topicName, message);
    }
}
