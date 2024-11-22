package com.agencify.authentication.dto;

import lombok.Data;

import java.util.Map;

/**
 * @author Titus Murithi Bundi
 */
@Data
public class NotificationEventHandlerDto {

    private String workflowId;

    private String subscriberId;

    private Map<String, Object> payload;
}
