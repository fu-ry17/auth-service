package com.agencify.authentication.events.producers;

import com.agencify.authentication.dto.NotificationEventHandlerDto;
import com.agencify.authentication.exception.error.JsonProcessingRuntimeException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * @author Titus Murithi Bundi
 */
@Component
@Slf4j
public class TriggerEventNotificationProducerEvent extends AbstractProducerEvent {


    public TriggerEventNotificationProducerEvent(KafkaTemplate<String, String> kafkaTemplate) {
        super(kafkaTemplate);
    }

    public void triggerEventNotification(NotificationEventHandlerDto payload) {
        var response = kafkaTemplate.send("global.triggerevent.notification", triggerEventNotificationStringtoPaylod(payload));
        handleKafkaResponse(response);
        log.info("Event sent to the trigger-event-notification topic");
    }

    private String triggerEventNotificationStringtoPaylod(NotificationEventHandlerDto payload) {
        try {
            return new ObjectMapper().writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new JsonProcessingRuntimeException("Error occurred while processing JSON", e);
        }
    }


}
