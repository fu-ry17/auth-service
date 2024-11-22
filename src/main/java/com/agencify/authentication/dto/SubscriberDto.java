package com.agencify.authentication.dto;

import lombok.Data;

/**
 * @author Titus Murithi Bundi
 */
@Data
public class SubscriberDto {
    String subscriberId;
    String firstName;
    String lastName;
    String email;
    String phone;
    String avatar;
    String locale;
    Object data;

}
