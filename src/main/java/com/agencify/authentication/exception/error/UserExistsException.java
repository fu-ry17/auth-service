package com.agencify.authentication.exception.error;

public class UserExistsException extends Exception {
    public UserExistsException(String message) {
        super(message);
    }
}
