package com.agencify.authentication.events.producers;

import com.agencify.authentication.dto.SendNotificationDto;
import com.agencify.authentication.model.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
@Slf4j
@Component
public class CreateAgentEvent {
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    public CreateAgentEvent(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void createAgent(User user) throws JsonProcessingException {
        kafkaTemplate.send(payloadToString(user), "create-user");
    }

    public void sendNotification(SendNotificationDto notification) {
        try {
            kafkaTemplate.send("notification", notificationPayloadToString(notification));
            log.info("Event sent to the notification service");
        } catch (IOException e) {
            log.info("Error occurred while sending notification. Error: {}", e.getMessage());
        }
    }

    private String payloadToString(User payload) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(payload);
    }

    private String notificationPayloadToString(SendNotificationDto notification) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(notification);
    }
}
