package com.agencify.authentication.services;

import com.agencify.authentication.exception.error.InvalidOtpException;
import com.agencify.authentication.exception.error.OTPException;
import com.agencify.authentication.exception.error.UserDoesNotExistsException;
import com.agencify.authentication.exception.error.UserExistsException;
import com.agencify.authentication.model.User;

public interface UserService {


    boolean createUnverifiedUser(User user) throws UserExistsException, UserDoesNotExistsException, OTPException;

    boolean verifyUser(String emailAddress, int otp) throws UserExistsException, InvalidOtpException, UserDoesNotExistsException;

    boolean resendSignUpOtp(String emailAddress) throws UserDoesNotExistsException, OTPException;

    User verifyUserEmail(String email);


    User updateUser(User user, int otp) throws UserDoesNotExistsException;

    boolean resetPasswordRequest(String emailAddress) throws UserDoesNotExistsException, OTPException;

    boolean verifyOtp(String emailAddress, int otp);

    boolean resetPassword(User user) throws UserDoesNotExistsException;

    boolean resetPasswordInApp(User user);
}
