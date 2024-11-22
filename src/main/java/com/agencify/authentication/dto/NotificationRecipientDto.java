package com.agencify.authentication.dto;

import lombok.Data;

@Data
public class NotificationRecipientDto {
    private String phoneNumber;

    private String emailAddress;

    private String name;
}
