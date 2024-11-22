package com.agencify.authentication.client;

import com.agencify.authentication.model.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "crm-setups-service")
public interface EntitiesClient {

    @PostMapping("/entities/register/agent")
    User register(@RequestBody User userRegistration);

    @GetMapping("/entities/get-agent/licence")
    User getAgentByLicenceNo(@RequestParam String licence);

    @GetMapping("/entities/verify/{email}")
    User verifyUser(@PathVariable String email);
}
