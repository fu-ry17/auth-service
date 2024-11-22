package com.agencify.authentication.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "otp-props")
public class OtpDefaultProperties {
    private String phoneNumber;
    private String otp;
}
