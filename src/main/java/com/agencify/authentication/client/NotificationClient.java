package com.agencify.authentication.client;

import com.agencify.authentication.dto.SendNotificationDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notifications-service")
public interface NotificationClient {

    @PostMapping("/notifications/send")
    void send(@RequestBody SendNotificationDto sendNotificationDto);
}
