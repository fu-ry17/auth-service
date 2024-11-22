package com.agencify.authentication.services.impl;


import com.agencify.authentication.client.EntitiesClient;
import com.agencify.authentication.client.NotificationClient;
import com.agencify.authentication.config.AppProperties;
import com.agencify.authentication.config.OtpDefaultProperties;
import com.agencify.authentication.dto.NotificationEventHandlerDto;
import com.agencify.authentication.dto.NotificationRecipientDto;
import com.agencify.authentication.dto.SendNotificationDto;
import com.agencify.authentication.dto.SubscriberDto;
import com.agencify.authentication.events.producers.CreateAgentEvent;
import com.agencify.authentication.events.producers.CreateSubscriberProducerEvent;
import com.agencify.authentication.events.producers.TriggerEventNotificationProducerEvent;
import com.agencify.authentication.exception.error.*;
import com.agencify.authentication.model.User;
import com.agencify.authentication.repository.UserRepository;
import com.agencify.authentication.services.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.Response;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Value("${keycloak-admin.username}")
    String username;

    @Value("${keycloak-admin.password}")
    String password;

    private final AppProperties appProperties;
    private final UserRepository userRepository;
    private final NotificationClient notificationClient;
    private final EntitiesClient entitiesClient;

    private final CreateAgentEvent createAgentEvent;

    private final CreateSubscriberProducerEvent createSubscriberProducerEvent;

    private final TriggerEventNotificationProducerEvent triggerEventNotificationProducerEvent;

    private final OtpDefaultProperties otpDefaultProperties;

    Keycloak keycloak;

    UsersResource usersResource;

    RealmResource realmResource;


    public UserServiceImpl(AppProperties appProperties, UserRepository userRepository, NotificationClient notificationClient, EntitiesClient entitiesClient, CreateAgentEvent createAgentEvent, CreateSubscriberProducerEvent createSubscriberProducerEvent, TriggerEventNotificationProducerEvent triggerEventNotificationProducerEvent, OtpDefaultProperties otpDefaultProperties) {
        this.appProperties = appProperties;
        this.userRepository = userRepository;
        this.notificationClient = notificationClient;
        this.entitiesClient = entitiesClient;
        this.createAgentEvent = createAgentEvent;
        this.createSubscriberProducerEvent = createSubscriberProducerEvent;
        this.triggerEventNotificationProducerEvent = triggerEventNotificationProducerEvent;


        this.otpDefaultProperties = otpDefaultProperties;
    }

    @Override
    public boolean createUnverifiedUser(User user) throws UserExistsException, UserDoesNotExistsException, OTPException {
        keycloak = getKeycakInstance();
        realmResource = keycloak.realm(appProperties.getRealm());
        usersResource = realmResource.users();
        Optional<UserRepresentation> keyCloakUser = usersResource.search(user.getEmailAddress()).stream().findFirst();


        if (keyCloakUser.isPresent()) {
            if (Boolean.TRUE.equals(keyCloakUser.get().isEnabled())) {
                throw new UserExistsException("A user with the same credentials already exists");
            }
        }

        String formattedPhoneNumber = user.getPhoneNumber().trim();
        if (formattedPhoneNumber.startsWith("0")) {
            formattedPhoneNumber = "+254" + formattedPhoneNumber.substring(1);
        }
        user.setPhoneNumber(formattedPhoneNumber);

        /*If user had attempted signup before*/
        if (keyCloakUser.isPresent()) {
            int otp = generateOTP(user.getPhoneNumber());
            User updatedUser = updateUser(user, otp);
            updatedUser.setPassword(user.getPassword());
            updatedUser.setOtp(otp);
            return sendOtpCode(updatedUser, otp);
        }
        /*Generate otp */
        int otp = generateOTP(user.getPhoneNumber());

        /*End of generate otp */


        /*Create user entity and organization id*/
        User agent = entitiesClient.register(user);

        //Publish create subscriber event
        createandsendSubscriber(agent);


        /*Creating the user on keycloak */
        agent.setPassword(user.getPassword());
        agent.setOtp(otp);
        agent.setFirstName(user.getFirstName());
        agent.setLastName(user.getLastName());
        User keycloakUser = createKeycloakUser(agent);
        /*End of creating user in keycloak*/

        //Publish event to send notification
        createandsendsubscriberOtp(agent);

        return sendOtpCode(keycloakUser, otp);
    }

    private void createandsendSubscriber(User agent) {
        SubscriberDto subscriberDto = new SubscriberDto();
        subscriberDto.setSubscriberId(agent.getEmailAddress().toLowerCase());
        subscriberDto.setFirstName(agent.getFirstName());
        subscriberDto.setLastName(agent.getLastName());
        subscriberDto.setEmail(agent.getEmailAddress().toLowerCase());
        subscriberDto.setPhone(agent.getPhoneNumber());
        subscriberDto.setAvatar(agent.getImageUrl());
        subscriberDto.setData(agent.getLicenceNo());
        subscriberDto.setLocale("en-US");
        createSubscriberProducerEvent.produceSubscriberEvent(subscriberDto);
    }

    private void createandsendsubscriberOtp(User agent) {
        NotificationEventHandlerDto notificationEventHandlerDto = new NotificationEventHandlerDto();
        notificationEventHandlerDto.setWorkflowId("create-subscriber-otp");

        notificationEventHandlerDto.setSubscriberId(agent.getEmailAddress());

        Map<String, Object> payload = new HashMap<>();
        payload.put("otp", agent.getOtp());
        notificationEventHandlerDto.setPayload(payload);

        triggerEventNotificationProducerEvent.triggerEventNotification(notificationEventHandlerDto);
    }

    @Override
    public boolean verifyUser(String emailAddress, int otp) throws InvalidOtpException, UserDoesNotExistsException {
        keycloak = getKeycakInstance();
        realmResource = keycloak.realm(appProperties.getRealm());
        usersResource = realmResource.users();

        Optional<UserRepresentation> keyCloakUser = usersResource.search(emailAddress).stream().findFirst();
        if (keyCloakUser.isPresent()) {
            UserRepresentation keyCloak = keyCloakUser.get();
            UserResource userResource = usersResource.get(keyCloak.getId());

            Map<String, List<String>> userAttributes = keyCloak.getAttributes();
            String sentOtp = userAttributes.get("otp").stream().findFirst().orElse(null);
            String sentOtpExpiry = userAttributes.get("otp_expiry").stream().findFirst().orElse(null);


            if ((sentOtp != null && sentOtpExpiry != null) && (sentOtp.equals(String.valueOf(otp)) && new Date().getTime() < Long.parseLong(sentOtpExpiry))) {
                keyCloak.setEnabled(true);
                userResource.update(keyCloak);
                return true;
            } else {
                throw new InvalidOtpException("Invalid otp. Please try again");
            }
        } else {
            throw new UserDoesNotExistsException("User does not exists. Please sign up");
        }
    }

    @Override
    public boolean resendSignUpOtp(String emailAddress) throws UserDoesNotExistsException, OTPException {
        keycloak = getKeycakInstance();
        realmResource = keycloak.realm(appProperties.getRealm());
        usersResource = realmResource.users();

        Optional<UserRepresentation> keyCloakUser = usersResource.search(emailAddress).stream().findFirst();

        if (keyCloakUser.isPresent()) {
            UserRepresentation keyCloak = keyCloakUser.get();
            UserResource userResource = usersResource.get(keyCloak.getId());

            //Generate new otp

            String phoneNumber = keyCloak.getAttributes().get("phoneNumber").getFirst();
            int newOtp = generateOTP(phoneNumber);

            // Update OTP and OTP expiry
            Map<String, List<String>> attributes = keyCloak.getAttributes();
            attributes.put("otp", Collections.singletonList(String.valueOf(newOtp)));
            attributes.put("otp_expiry", Collections.singletonList(String.valueOf(new Date().getTime() + 180000)));
            keyCloak.setAttributes(attributes);
            userResource.update(keyCloak);

            //send new OTP
            User user = new User();
            user.setEmailAddress(emailAddress);
            user.setPhoneNumber(phoneNumber);
            user.setOtp(newOtp);
            return sendOtpCode(user, newOtp);
        } else {
            throw new UserDoesNotExistsException("User does not exists. Please sign up");
        }

    }

    @Override
    public User verifyUserEmail(String email) {
        log.info("in the email address");
        return entitiesClient.verifyUser(email);

    }

    @Override
    public User updateUser(User user, int otp) throws UserDoesNotExistsException {
        User agent = entitiesClient.register(user);

        //Publish create subscriber event to add firstName and lastName
        createandsendSubscriber(agent);

        keycloak = getKeycakInstance();
        realmResource = keycloak.realm(appProperties.getRealm());
        usersResource = realmResource.users();

        Map<String, List<String>> map = new HashMap<>();
        Optional<UserRepresentation> keyCloakUser = usersResource.search(user.getEmailAddress()).stream().findFirst();

        if (keyCloakUser.isPresent()) {
            UserRepresentation keyCloak = keyCloakUser.get();
            UserResource userResource = usersResource.get(keyCloak.getId());
            keyCloak.setEnabled(true);
            keyCloak.setUsername(user.getEmailAddress());
            keyCloak.setFirstName(user.getFirstName());
            keyCloak.setLastName(user.getLastName());
            keyCloak.setEmail(user.getEmailAddress());
            if (agent.getPhoneNumber() != null) {
                map.put("phoneNumber", Collections.singletonList(agent.getPhoneNumber()));
            }
            map.put("imageUrl", Collections.singletonList(agent.getImageUrl()));
            map.put("licenceNo", Collections.singletonList(user.getLicenceNo()));
            map.put("organizationId", Collections.singletonList(agent.getOrganizationId().toString()));
            map.put("otp", Collections.singletonList(String.valueOf(otp)));
            map.put("otp_expiry", Collections.singletonList(String.valueOf(new Date().getTime() + 180000)));
//            map.put("subscriberId", Collections.singletonList(keyCloak.getRealmRoles().get(0) + keyCloak.getId()));
            keyCloak.setAttributes(map);
            userResource.update(keyCloak);
            var role = userResource.roles().getAll().getRealmMappings().getFirst().toString();
            var subscriberId = role + keyCloak.getId();

            // sendWelcomeEmailEvent(user);

            return agent;
        }

        throw new UserDoesNotExistsException("User not found. Please create an account");
    }


    public void sendWelcomeEmailEvent(User user) {
        log.info("in the email event");
        SendNotificationDto sendNotificationDto = new SendNotificationDto();

        NotificationRecipientDto notificationRecipientDto = new NotificationRecipientDto();
        notificationRecipientDto.setEmailAddress(user.getEmailAddress());
        notificationRecipientDto.setPhoneNumber(user.getPhoneNumber());
        notificationRecipientDto.setName(user.getFirstName());

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("email", user.getEmailAddress());
        attributes.put("phoneNumber", user.getPhoneNumber());
        log.info("in the email 2");
        attributes.put("firstName", user.getFirstName());
        attributes.put("lastName", user.getLastName());
        sendNotificationDto.setAttributes(attributes);
        sendNotificationDto.setNotificationRecipient(notificationRecipientDto);
        sendNotificationDto.setTemplateShortCode("WELCOME_EMAIL");
        sendNotificationDto.setChannels("email");
        sendNotificationDto.setDelivery("now");
        sendNotificationDto.setSubject("Welcome to Agencify");
        sendNotificationDto.setMessage("");

        createAgentEvent.sendNotification(sendNotificationDto);


    }

    @Override
    public boolean resetPasswordRequest(String emailAddress) throws UserDoesNotExistsException, OTPException {

        // Decode the URL-encoded email address
        String decodedEmailAddress = java.net.URLDecoder.decode(emailAddress, StandardCharsets.UTF_8);

        // Trim the email address
        String trimmedEmailAddress = decodedEmailAddress.trim();

        keycloak = getKeycakInstance();
        realmResource = keycloak.realm(appProperties.getRealm());
        usersResource = realmResource.users();

        Optional<UserRepresentation> keyCloakUser = usersResource.search(trimmedEmailAddress).stream().findFirst();

        if (keyCloakUser.isPresent()) {
            UserRepresentation keyCloak = keyCloakUser.get();
            User user = new User();
            user.setEmailAddress(keyCloak.getEmail());
            user.setKeycloakId(keyCloak.getId());
            Map<String, List<String>> attr = keyCloak.getAttributes();
            user.setPhoneNumber(attr.get("phoneNumber").getFirst());
            int otp = generateOTP(user.getPhoneNumber());
            log.info("OTP: {}", otp);
            user.setOtp(otp);
            userRepository.save(user);
            return sendOtpCode(user, otp);
        }

        throw new UserDoesNotExistsException("User not found. Please create an account");
    }

    @Override
    public boolean verifyOtp(String emailAddress, int otp) {
        User updateUser = userRepository.findTopByEmailAddressOrderByIdDesc(emailAddress);

        log.info("User from db: {}", updateUser);

        return updateUser.getOtp() == otp;
    }


    @SneakyThrows
    @Override
    public boolean resetPassword(User user) throws UserDoesNotExistsException {

        log.info("User to reset password: {}", new ObjectMapper().writeValueAsString(user));
        keycloak = getKeycakInstance();
        realmResource = keycloak.realm(appProperties.getRealm());
        usersResource = realmResource.users();

        var keycloakUser = usersResource.search(user.getEmailAddress()).stream().findFirst().orElse(null);

        log.info("User from keycloak: {}", new ObjectMapper().writeValueAsString(keycloakUser));

        if (keycloakUser != null) {

            // Define password credential
            CredentialRepresentation passwordCred = new CredentialRepresentation();

            passwordCred.setTemporary(false);
            passwordCred.setType(CredentialRepresentation.PASSWORD);
            passwordCred.setValue(user.getPassword());

            log.info(" User Password: {}", passwordCred.getValue());

            UserResource userResource = usersResource.get(keycloakUser.getId());


            try {
                // Set password credential
                userResource.resetPassword(passwordCred);
            } catch (RuntimeException exception) {
                log.error("Password not accepted", exception);

                if (Objects.equals(user.getPassword(), user.getEmailAddress())) {
                    throw new InvalidPasswordException("The password cannot be an email address", exception.getCause());
                } else if (user.getPassword().length() < 8) {
                    throw new InvalidPasswordException("The password length cannot be less than 8 characters", exception.getCause());
                } else if (Objects.equals(user.getPassword(), keycloakUser.getUsername())) {
                    throw new InvalidPasswordException("The password cannot be the same as your username", exception.getCause());

                } else {
                    throw new InvalidPasswordException("The new password cannot be the same as the old password", exception.getCause());

                }

            }

            return true;
        } else {
            throw new UserDoesNotExistsException("User not found. Please create an account");
        }


    }

    @Override
    public boolean resetPasswordInApp(User user) {
        return true;
    }

    public User createKeycloakUser(User user) throws UserExistsException {
        try {
            log.info("User creation: {}", new ObjectMapper().writeValueAsString(user));
        } catch (JsonProcessingException e) {
            log.info("context", e);
        }
        Keycloak keycloak = getKeycakInstance();
        RealmResource realmResource = keycloak.realm(appProperties.getRealm());
        UsersResource usersResource = realmResource.users();
        Map<String, List<String>> map = new HashMap<>();
        UserRepresentation keyCloak = new UserRepresentation();
        keyCloak.setEnabled(false);
        keyCloak.setUsername(user.getEmailAddress());
        keyCloak.setFirstName(user.getFirstName());
        keyCloak.setLastName(user.getLastName());
        keyCloak.setEmail(user.getEmailAddress());
        map.put("phoneNumber", Collections.singletonList(user.getPhoneNumber()));
        map.put("imageUrl", Collections.singletonList(user.getImageUrl()));
        map.put("licenceNo", Collections.singletonList(user.getLicenceNo()));
        map.put("organizationId", Collections.singletonList(user.getOrganizationId().toString()));
        map.put("otp", Collections.singletonList(String.valueOf(user.getOtp())));
        map.put("otp_expiry", Collections.singletonList(String.valueOf(new Date().getTime() + 180000)));
        keyCloak.setAttributes(map);

        CredentialRepresentation passwordCred = new CredentialRepresentation();
        passwordCred.setTemporary(false);
        passwordCred.setType(CredentialRepresentation.PASSWORD);
        passwordCred.setValue(user.getPassword());
        keyCloak.setCredentials(Collections.singletonList(passwordCred));
        Response response = usersResource.create(keyCloak);

        var userId = CreatedResponseUtil.getCreatedId(response);
        var userResource = usersResource.get(userId);

        var realmRoleUser = realmResource.roles().get(user.getRole()).toRepresentation();
        userResource.roles().realmLevel().add(Collections.singletonList(realmRoleUser));

        log.info("User creation: response {}", response.getStatusInfo());
        if (response.getStatus() == Response.Status.CONFLICT.getStatusCode()) {
            throw new UserExistsException("User already exists. Please login");
        }
        return user;
    }

    protected Keycloak getKeycakInstance() {
        return KeycloakBuilder.builder().serverUrl(appProperties.getAuthServerUrl()).realm("master").grantType(OAuth2Constants.PASSWORD).clientId("admin-cli").username(username).password(password).build();
    }

    private boolean sendOtpCode(User user, int otp) throws OTPException {
        SendNotificationDto sendNotificationDto = new SendNotificationDto();
        NotificationRecipientDto notificationRecipientDto = new NotificationRecipientDto();
        notificationRecipientDto.setEmailAddress(user.getEmailAddress());
        notificationRecipientDto.setPhoneNumber(user.getPhoneNumber());
        notificationRecipientDto.setName(null);
        sendNotificationDto.setChannels("sms");
        sendNotificationDto.setMessage("Your one time password is " + otp);
        sendNotificationDto.setSubject("OTP");
        sendNotificationDto.setNotificationRecipient(notificationRecipientDto);

        try {
            notificationClient.send(sendNotificationDto);
        } catch (Exception e) {
            log.error("OTP sending to " + user.getPhoneNumber() + " failed. Cause: " + e.getLocalizedMessage());
            throw new OTPException("OTP Error");
        }

        return true;
    }

    public int generateOTP(String phoneNumber) {
        if (Objects.equals(phoneNumber.substring(phoneNumber.length() - 8),
                otpDefaultProperties.getPhoneNumber().substring(otpDefaultProperties.getPhoneNumber().length() - 8))) {
            return Integer.parseInt(otpDefaultProperties.getOtp());
        }
        Random random = new Random();
        return 1000 + random.nextInt(9000);
    }

}
