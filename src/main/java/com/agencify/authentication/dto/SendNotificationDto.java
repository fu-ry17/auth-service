package com.agencify.authentication.dto;

import lombok.Data;

import java.util.Map;

@Data
public class SendNotificationDto {
    private String message;

    private String subject;

    private String channels = "sms";

    private String delivery = "now";

    private NotificationRecipientDto notificationRecipient;

    private Map<String, Object> attributes;

    private String templateCode;

    private String templateShortCode;


}
