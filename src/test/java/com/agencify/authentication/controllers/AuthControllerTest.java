package com.agencify.authentication.controllers;

import com.agencify.authentication.exception.error.InvalidOtpException;
import com.agencify.authentication.exception.error.OTPException;
import com.agencify.authentication.exception.error.UserDoesNotExistsException;
import com.agencify.authentication.exception.error.UserExistsException;
import com.agencify.authentication.model.User;
import com.agencify.authentication.services.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Random;

class AuthControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    AuthController authController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createUnverifiedUser() throws UserExistsException, OTPException, UserDoesNotExistsException {
        User user = new User();
        user.setOtp(4578);
        String forOtp = "steikdsnfkmkf0";
        int rand = generateOTP(forOtp);
        user.setOrganizationId(123L);
        user.setRole("ROLE_AGENT");
        user.setPassword("12345678test");
        Mockito.when(userService.createUnverifiedUser(user)).thenReturn(true);
        // Assertions.assertTrue(authController.createUnverifiedUser(user));
    }

    @Test
    void updateVerifiedUser() throws UserDoesNotExistsException {
        User user = new User();
        user.setOtp(4578);
        String forOtp = "steikdsnfkmkf0";
        int rand = generateOTP(forOtp);
        user.setOrganizationId(123L);
        user.setRole("ROLE_AGENT");
        user.setPassword("12345678test");
        Mockito.when(userService.updateUser(user, rand)).thenReturn(user);
        //User results = authController.updateVerifiedUser(user);

    }

    @Test
    void verifyUser() throws UserExistsException, UserDoesNotExistsException, InvalidOtpException {
        User user = new User();
        user.setOtp(4578);
        String forOtp = "steikdsnfkmkf0";
        int rand = generateOTP(forOtp);
        user.setOrganizationId(123L);
        user.setRole("ROLE_AGENT");
        user.setPassword("12345678test");
        Mockito.when(userService.verifyUser(user.getEmailAddress(), rand)).thenReturn(true);
        Assertions.assertTrue(authController.verifyUser(user.getEmailAddress(), rand));

    }

    @Test
    void verifyAgentEmail() throws UserDoesNotExistsException {

        User user = new User();
        user.setOtp(4578);
        String forOtp = "steikdsnfkmkf0";
        user.setOrganizationId(123L);
        user.setRole("ROLE_AGENT");
        user.setPassword("12345678test");
        Mockito.when(userService.verifyUserEmail(user.getEmailAddress())).thenReturn(user);
        ResponseEntity results = authController.verifyAgentEmail(user.getEmailAddress());
        Assertions.assertEquals(HttpStatus.OK, results.getStatusCode());
        System.out.println(results);
    }

    @Test
    void resetPasswordRequest() throws OTPException, UserDoesNotExistsException {
        User user = new User();
        user.setOtp(4578);
        user.setOrganizationId(123L);
        user.setRole("ROLE_AGENT");
        user.setPassword("12345678test");
        Mockito.when(userService.resetPasswordRequest(user.getEmailAddress())).thenReturn(true);
        Assertions.assertTrue(authController.resetPasswordRequest(user.getEmailAddress()));


    }

    @Test
    void verifyOtp() {
        User user = new User();
        user.setOtp(4578);
        String forOtp = "steikdsnfkmkf0";
        int rand = generateOTP(forOtp);
        user.setOrganizationId(123L);
        user.setRole("ROLE_AGENT");
        user.setPassword("12345678test");
        Mockito.when(userService.verifyOtp(user.getEmailAddress(), rand)).thenReturn(true);
        Assertions.assertTrue(authController.verifyOtp(user.getEmailAddress(), rand));
    }

    @Test
    void resetPassword() throws UserDoesNotExistsException {
        User user = new User();
        user.setOtp(4578);
        user.setOrganizationId(123L);
        user.setRole("ROLE_AGENT");
        user.setPassword("12345678test");
        Mockito.when(userService.resetPassword(user)).thenReturn(true);
        Assertions.assertTrue(authController.resetPassword(user));


    }

    @Test
    void resetPasswordInApp() throws UserDoesNotExistsException {
        User user = new User();
        user.setOtp(4578);
        user.setOrganizationId(123L);
        user.setRole("ROLE_AGENT");
        user.setPassword("12345678test");

        Mockito.when(userService.resetPasswordInApp(user)).thenReturn(true);
        Assertions.assertFalse(authController.resetPasswordInApp(user));
    }


    public int generateOTP(String key) {
        Random random = new Random();
        return 1000000 + random.nextInt(9000);
    }
}