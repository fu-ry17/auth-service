package com.agencify.authentication.exception.error;

public class InvalidPasswordException extends RuntimeException{

    public InvalidPasswordException(String message, Throwable cause) {
        super(message, cause);
    }
}
