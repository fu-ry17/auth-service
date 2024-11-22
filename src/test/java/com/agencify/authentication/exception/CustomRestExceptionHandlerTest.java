package com.agencify.authentication.exception;

import com.agencify.authentication.exception.enums.ErrorType;
import com.agencify.authentication.exception.error.*;
import com.agencify.authentication.exception.payload.ApiError;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class CustomRestExceptionHandlerTest {

    @Test
    void handleUserExistsException() {

        // Create a mock UserExistsException
        UserExistsException userExistsException = Mockito.mock(UserExistsException.class);
        String exceptionMessage = "User already exists";
        Mockito.when(userExistsException.getMessage()).thenReturn(exceptionMessage);

        // Create an instance of YourClass
        CustomRestExceptionHandler yourClass = new CustomRestExceptionHandler();

        // Call the handleUserExistsException method
        ResponseEntity<Object> responseEntity = yourClass.handleUserExistsException(userExistsException);

        // Assert that the response status is HttpStatus.CONFLICT
        Assertions.assertEquals(HttpStatus.CONFLICT, responseEntity.getStatusCode());

        // Assert that the response body contains the expected data
        ApiError expectedApiError = new ApiError(
                HttpStatus.CONFLICT,
                ErrorType.ERROR,
                exceptionMessage,
                Collections.singletonList(exceptionMessage),
                userExistsException.toString(),
                "AU001"
        );
        Assertions.assertEquals(expectedApiError, responseEntity.getBody());

    }

    @Test
    void handleUserDoesNotExistsException() {

        // Create a mock UserDoesNotExistsException
        UserDoesNotExistsException userDoesNotExistsException = Mockito.mock(UserDoesNotExistsException.class);
        String exceptionMessage = "User does not exist";
        Mockito.when(userDoesNotExistsException.getMessage()).thenReturn(exceptionMessage);

        // Create an instance of YourClass
        CustomRestExceptionHandler yourClass = new CustomRestExceptionHandler();

        // Call the handleUserDoesNotExistsException method
        ResponseEntity<Object> responseEntity = yourClass.handleUserDoesNotExistsException(userDoesNotExistsException);

        // Assert that the response status is HttpStatus.NOT_FOUND
        Assertions.assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());

        // Assert that the response body contains the expected data
        ApiError expectedApiError = new ApiError(
                HttpStatus.NOT_FOUND,
                ErrorType.ERROR,
                exceptionMessage,
                Collections.singletonList(exceptionMessage),
                userDoesNotExistsException.toString(),
                "AU002"
        );
        Assertions.assertEquals(expectedApiError, responseEntity.getBody());
    }


    @Test
    void handleInvalidOtpException() {

        // Create a mock InvalidOtpException
        InvalidOtpException invalidOtpException = Mockito.mock(InvalidOtpException.class);
        String exceptionMessage = "Invalid OTP";
        Mockito.when(invalidOtpException.getMessage()).thenReturn(exceptionMessage);

        // Create an instance of YourClass
        CustomRestExceptionHandler yourClass = new CustomRestExceptionHandler();

        // Call the handleInvalidOtpException method
        ResponseEntity<Object> responseEntity = yourClass.handleInvalidOtpException(invalidOtpException);

        // Assert that the response status is HttpStatus.BAD_REQUEST
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());

        // Assert that the response body contains the expected data
        ApiError expectedApiError = new ApiError(
                HttpStatus.BAD_REQUEST,
                ErrorType.ERROR,
                exceptionMessage,
                Collections.singletonList(exceptionMessage),
                invalidOtpException.toString(),
                "AU003"
        );
        Assertions.assertEquals(expectedApiError, responseEntity.getBody());
    }

    @Test
    void testHandleInvalidOtpException() {

        OTPException otpException = Mockito.mock(OTPException.class);
        String exceptionMessage = "Invalid OTP";
        Mockito.when(otpException.getMessage()).thenReturn(exceptionMessage);

        // Create an instance of YourClass
        CustomRestExceptionHandler yourClass = new CustomRestExceptionHandler();

        // Call the handleInvalidOtpException method
        ResponseEntity<Object> responseEntity = yourClass.handleInvalidOtpException(otpException);

        // Assert that the response status is HttpStatus.BAD_REQUEST
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());

        // Assert that the response body contains the expected data
        ApiError expectedApiError = new ApiError(
                HttpStatus.BAD_REQUEST,
                ErrorType.ERROR,
                exceptionMessage,
                Collections.singletonList(exceptionMessage),
                otpException.toString(),
                "AU003"
        );
        Assertions.assertEquals(expectedApiError, responseEntity.getBody());
    }


    @Test
    void handleLicenceNoExistsException() {
        // Create a mock LicenceNoExistsException
        LicenceNoExistsException licenceNoExistsException = Mockito.mock(LicenceNoExistsException.class);
        String exceptionMessage = "Licence number does not exist";
        Mockito.when(licenceNoExistsException.getMessage()).thenReturn(exceptionMessage);

        // Create an instance of YourClass
        CustomRestExceptionHandler yourClass = new CustomRestExceptionHandler();

        // Call the handleLicenceNoExistsException method
        ResponseEntity<Object> responseEntity = yourClass.handleLicenceNoExistsException(licenceNoExistsException);

        // Assert that the response status is HttpStatus.NOT_FOUND
        Assertions.assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());

        // Assert that the response body contains the expected data
        ApiError expectedApiError = new ApiError(
                HttpStatus.NOT_FOUND,
                ErrorType.ERROR,
                exceptionMessage,
                Collections.singletonList(exceptionMessage),
                licenceNoExistsException.toString(),
                "AU004"
        );
        Assertions.assertEquals(expectedApiError, responseEntity.getBody());
    }




    @Test
    void handleInvalidPasswordException() {

        // Create a mock InvalidPasswordException
        InvalidPasswordException invalidPasswordException = Mockito.mock(InvalidPasswordException.class);
        String exceptionMessage = "Invalid password";
        Mockito.when(invalidPasswordException.getMessage()).thenReturn(exceptionMessage);

        // Create an instance of YourClass
        CustomRestExceptionHandler yourClass = new CustomRestExceptionHandler();

        // Call the handleInvalidPasswordException method
        ResponseEntity<Object> responseEntity = yourClass.handleInvalidPasswordException(invalidPasswordException);

        // Assert that the response status is HttpStatus.BAD_REQUEST
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());

        // Assert that the response body contains the expected data
        ApiError expectedApiError = new ApiError(
                HttpStatus.BAD_REQUEST,
                ErrorType.ERROR,
                exceptionMessage,
                Collections.singletonList(exceptionMessage),
                invalidPasswordException.toString(),
                "AU005"
        );
        Assertions.assertEquals(expectedApiError, responseEntity.getBody());
    }

}