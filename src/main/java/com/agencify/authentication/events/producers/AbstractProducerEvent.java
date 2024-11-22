package com.agencify.authentication.events.producers;

import com.agencify.authentication.exception.error.JsonProcessingRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

/**
 * @author Titus Murithi Bundi
 */
@Slf4j
public abstract class AbstractProducerEvent {

    protected final KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    public AbstractProducerEvent(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    protected void handleKafkaResponse(CompletableFuture<SendResult<String, String>> response) {
        response.thenAccept(sendResult -> {
            log.info("Message sent successfully!");
            log.info("Topic: " + sendResult.getRecordMetadata().topic());
            log.info("Partition: " + sendResult.getRecordMetadata().partition());
            log.info("Offset: " + sendResult.getRecordMetadata().offset());
        }).exceptionally(throwable -> {
            log.error("An error occurred while sending the message ERROR: {}", throwable.getMessage());
            throw new JsonProcessingRuntimeException("An error occurred while sending the message", throwable);
        });
    }
}
