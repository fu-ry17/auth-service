package com.agencify.authentication.exception.payload;

import com.agencify.authentication.exception.enums.ErrorType;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.util.List;

@Data
@AllArgsConstructor
public class ApiError {
    private HttpStatus status;
    private ErrorType errorType;
    private String message;
    private List<String> errors;
    private String developerMessage;
    private String errorCode;
}

