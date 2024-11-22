package com.agencify.authentication;

import com.agencify.authentication.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableDiscoveryClient
@EnableFeignClients
@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class TqauthenticationApplication {

    public static void main(String[] args) {
        SpringApplication.run(TqauthenticationApplication.class, args);
    }

}
