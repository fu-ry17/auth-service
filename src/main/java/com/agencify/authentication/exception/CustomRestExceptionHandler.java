package com.agencify.authentication.exception;

import com.agencify.authentication.exception.enums.ErrorType;
import com.agencify.authentication.exception.error.*;
import com.agencify.authentication.exception.payload.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Collections;

@RestControllerAdvice
public class CustomRestExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * @param userExistsException
     * @return
     */
    @ExceptionHandler(UserExistsException.class)
    public ResponseEntity<Object> handleUserExistsException(UserExistsException userExistsException) {

        ApiError apiError = new ApiError(
                HttpStatus.CONFLICT,
                ErrorType.ERROR,
                userExistsException.getMessage(),
                Collections.singletonList(userExistsException.getMessage()),
                userExistsException.toString(),
                "AU001"
        );

        return new ResponseEntity<>(apiError, apiError.getStatus());
    }

    /**
     * @param userDoesNotExistsException
     * @return
     */
    @ExceptionHandler(UserDoesNotExistsException.class)
    public ResponseEntity<Object> handleUserDoesNotExistsException(UserDoesNotExistsException userDoesNotExistsException) {

        ApiError apiError = new ApiError(
                HttpStatus.NOT_FOUND,
                ErrorType.ERROR,
                userDoesNotExistsException.getMessage(),
                Collections.singletonList(userDoesNotExistsException.getMessage()),
                userDoesNotExistsException.toString(),
                "AU002"
        );

        return new ResponseEntity<>(apiError, apiError.getStatus());
    }

    /**
     * @param invalidOtpException
     * @return
     */
    @ExceptionHandler(InvalidOtpException.class)
    public ResponseEntity<Object> handleInvalidOtpException(InvalidOtpException invalidOtpException) {

        ApiError apiError = new ApiError(
                HttpStatus.BAD_REQUEST,
                ErrorType.ERROR,
                invalidOtpException.getMessage(),
                Collections.singletonList(invalidOtpException.getMessage()),
                invalidOtpException.toString(),
                "AU003"
        );

        return new ResponseEntity<>(apiError, apiError.getStatus());
    }

    /**
     * @param otpException
     * @return
     */
    @ExceptionHandler(OTPException.class)
    public ResponseEntity<Object> handleInvalidOtpException(OTPException otpException) {

        ApiError apiError = new ApiError(
                HttpStatus.BAD_REQUEST,
                ErrorType.ERROR,
                otpException.getMessage(),
                Collections.singletonList(otpException.getMessage()),
                otpException.toString(),
                "AU003"
        );

        return new ResponseEntity<>(apiError, apiError.getStatus());
    }

    /**
     * Handles agent licence No exceptions
     *
     * @param licenceNoExistsException the licenceNoExistsException Runtime Exception
     * @return ApiError
     */
    @ExceptionHandler(LicenceNoExistsException.class)
    public ResponseEntity<Object> handleLicenceNoExistsException(LicenceNoExistsException licenceNoExistsException) {
        ApiError apiError = new ApiError(
                HttpStatus.NOT_FOUND,
                ErrorType.ERROR,
                licenceNoExistsException.getMessage(),
                Collections.singletonList(licenceNoExistsException.getMessage()),
                licenceNoExistsException.toString(),
                "AU004"
        );

        return new ResponseEntity<>(apiError, apiError.getStatus());
    }


    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<Object> handleInvalidPasswordException(InvalidPasswordException invalidPasswordException) {
        ApiError apiError = new ApiError(
                HttpStatus.BAD_REQUEST,
                ErrorType.ERROR,
                invalidPasswordException.getMessage(),
                Collections.singletonList(invalidPasswordException.getMessage()),
                invalidPasswordException.toString(),
                "AU005"
        );

        return new ResponseEntity<>(apiError, apiError.getStatus());
    }

    @ExceptionHandler(JsonProcessingRuntimeException.class)
    public ResponseEntity<Object> handleJsonProcessingRuntimeException(JsonProcessingRuntimeException jsonProcessingRuntimeException) {
        ApiError apiError = new ApiError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorType.ERROR,
                jsonProcessingRuntimeException.getMessage(),
                Collections.singletonList(jsonProcessingRuntimeException.getMessage()),
                jsonProcessingRuntimeException.toString(),
                "AU006"
        );

        return new ResponseEntity<>(apiError, apiError.getStatus());
    }
}
