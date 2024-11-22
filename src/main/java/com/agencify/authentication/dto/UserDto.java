package com.agencify.authentication.dto;

import lombok.Data;

@Data
public class UserDto {

    private String email;

    private String firstName;

    private String lastName;

    private String imageUrl;

    private String phoneNumber;

    private Long organizationId;

    private String licenceNo;
}
