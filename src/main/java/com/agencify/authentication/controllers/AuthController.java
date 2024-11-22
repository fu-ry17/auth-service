package com.agencify.authentication.controllers;

import com.agencify.authentication.exception.error.InvalidOtpException;
import com.agencify.authentication.exception.error.OTPException;
import com.agencify.authentication.exception.error.UserDoesNotExistsException;
import com.agencify.authentication.exception.error.UserExistsException;
import com.agencify.authentication.model.User;
import com.agencify.authentication.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.cloud.context.config.annotation.RefreshScope;


@Slf4j
@RestController
@RefreshScope
@RequestMapping("users")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    //    @RolesAllowed("auth_create")
    @PostMapping("/create")
    public boolean createUnverifiedUser(@RequestBody User user) throws UserExistsException, UserDoesNotExistsException, OTPException {
        return userService.createUnverifiedUser(user);
    }

    //    @RolesAllowed("auth_update")
    @PutMapping("/update")
    public User updateVerifiedUser(@RequestBody User user) throws UserDoesNotExistsException {
        return userService.updateUser(user, 0);
    }

    @PostMapping("/regenerate-signup-otp")
    public ResponseEntity<Object> resendOtp(@RequestParam String emailAddress) {
        try {
            boolean result = userService.resendSignUpOtp(emailAddress);
            return ResponseEntity.ok(result);
        } catch (UserDoesNotExistsException | OTPException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    //    @RolesAllowed("auth_verify_usr")
    @PostMapping("verify-registration-otp")
    public boolean verifyUser(@RequestParam("emailAddress") String emailAddress,
                              @RequestParam("otp") int otp) throws UserExistsException, InvalidOtpException, UserDoesNotExistsException {
        return userService.verifyUser(emailAddress, otp);
    }


    @GetMapping("/{emailAddress}/verify")
    public ResponseEntity verifyAgentEmail(@PathVariable String emailAddress) {
        log.info(emailAddress);
        return ResponseEntity.ok(userService.verifyUserEmail(emailAddress));
    }

    //    @RolesAllowed("auth_reset_pwd")
    @PostMapping("reset-password-request")
    public boolean resetPasswordRequest(@RequestParam("emailAddress") String emailAddress) throws UserDoesNotExistsException, OTPException {
        return userService.resetPasswordRequest(emailAddress);
    }

    @PostMapping("regenerate-otp")
    public boolean regenerateOtp(@RequestParam("emailAddress") String emailAddress) throws UserDoesNotExistsException, OTPException {
        return userService.resetPasswordRequest(emailAddress);
    }

    //    @RolesAllowed("auth_verify_otp")
    @PostMapping("verify-reset-otp")
    public boolean verifyOtp(@RequestParam("emailAddress") String emailAddress,
                             @RequestParam("otp") int otp) {
        return userService.verifyOtp(emailAddress, otp);
    }

    //    @RolesAllowed("auth_reset_pwd")
    @PostMapping("reset-password")
    public boolean resetPassword(@RequestBody User user) throws UserDoesNotExistsException {
        return userService.resetPassword(user);
    }

    //    @RolesAllowed("auth_reset_pwd")
    @PostMapping("inapp-reset-password")
    public boolean resetPasswordInApp(@RequestBody User user) throws UserDoesNotExistsException {
        return userService.resetPassword(user);
    }
}
