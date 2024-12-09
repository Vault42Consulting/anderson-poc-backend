package com.anderson.demo.service;

import com.anderson.demo.event.ContactEvent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(value = "spring.kafka.enabled", havingValue = "true")
public class KafkaEventService {
    private final KafkaTemplate<String, ContactEvent> kafkaTemplate;
    private final String topic;

    public KafkaEventService(
            KafkaTemplate<String, ContactEvent> kafkaTemplate,
            @Value("${app.kafka.topic}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    public void sendEvent(ContactEvent event) {
        kafkaTemplate.send(topic, event.getUserId(), event);
    }
}
