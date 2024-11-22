package com.agencify.authentication.events.producers;

import com.agencify.authentication.dto.SubscriberDto;
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
public class CreateSubscriberProducerEvent extends AbstractProducerEvent {

    public CreateSubscriberProducerEvent(KafkaTemplate<String, String> kafkaTemplate) {
        super(kafkaTemplate);
    }

    public void produceSubscriberEvent(SubscriberDto payload) {
        var response = kafkaTemplate.send("global.createsubscriber.notification", createSubscriberPayloadToString(payload));
        handleKafkaResponse(response);
        log.info("Event sent to the create-subscriber topic");
    }

    private String createSubscriberPayloadToString(SubscriberDto subscriberDto) {
        try {
            return new ObjectMapper().writeValueAsString(subscriberDto);
        } catch (JsonProcessingException e) {
            throw new JsonProcessingRuntimeException("Error occurred while processing JSON", e);
        }
    }

}
