package com.agencify.authentication.events.producers;

import com.agencify.authentication.dto.SendNotificationDto;
import com.agencify.authentication.model.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.kafka.core.KafkaTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CreateAgentEventTest {

    private CreateAgentEvent createAgentEvent;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Captor
    private ArgumentCaptor<String> payloadCaptor;
    @Captor
    private ArgumentCaptor<String> topicCaptor;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        createAgentEvent = new CreateAgentEvent(kafkaTemplate);
    }

    @Test
    void createAgent() throws JsonProcessingException {





                // Mock the KafkaTemplate's send method
                String expectedPayload = "{\"id\":null,\"emailAddress\":null,\"firstName\":\"John Doe\",\"lastName\":null,\"imageUrl\":null,\"password\":null,\"keycloakId\":null,\"phoneNumber\":null,\"organizationId\":null,\"licenceNo\":null,\"otp\":0,\"role\":\"agent\"}";
                String expectedTopic = "create-user";
                when(kafkaTemplate.send(topicCaptor.capture(), payloadCaptor.capture())).thenReturn(null);

                // Create a test user
                User user = new User();

                user.setFirstName("John Doe");

                // Call the createAgent method
                createAgentEvent.createAgent(user);

                // Verify the KafkaTemplate's send method was called with the expected payload and topic
                verify(kafkaTemplate).send(topicCaptor.capture(), payloadCaptor.capture());

                // Assert the captured payload and topic

                assertEquals(expectedTopic, payloadCaptor.getValue());
//        assertEquals(expectedPayload, );
            }


    @Test
    void sendNotification() {


        String expectedPayload = "{\"message\":\"Test notification\",\"subject\":null,\"channels\":\"sms\",\"delivery\":\"now\",\"notificationRecipient\":null,\"attributes\":null,\"templateCode\":null,\"templateShortCode\":null}";
        String expectedTopic = "notification";
        when(kafkaTemplate.send(topicCaptor.capture(), payloadCaptor.capture())).thenReturn(null);

        // Create a test notification
        SendNotificationDto notification = new SendNotificationDto();
//        notification.setId(1);
        notification.setMessage("Test notification");

        // Call the sendNotification method
        createAgentEvent.sendNotification(notification);

        // Verify the KafkaTemplate's send method was called with the expected payload and topic
        verify(kafkaTemplate).send(topicCaptor.capture(), payloadCaptor.capture());

        // Assert the captured payload and topic
        assertEquals(expectedPayload, payloadCaptor.getValue());
        assertEquals(expectedTopic, topicCaptor.getValue());
    }

    }
