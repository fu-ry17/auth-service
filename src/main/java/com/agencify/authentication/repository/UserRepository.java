package com.agencify.authentication.repository;

import com.agencify.authentication.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    User findDistinctByPhoneNumber(String phoneNumber);

    //Use findTopByEmailAddressOrderByIdDesc
    //Will result to Non unique result exception
    User findDistinctByEmailAddress(String emailAddress);

    User findTopByEmailAddressOrderByIdDesc(String emailAddress);

    Optional<User> findDistinctByPhoneNumberOrEmailAddress(String phoneNumber, String emailAddress);
}
