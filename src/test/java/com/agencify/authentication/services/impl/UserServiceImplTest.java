package com.agencify.authentication.services.impl;

import com.agencify.authentication.client.EntitiesClient;
import com.agencify.authentication.client.NotificationClient;
import com.agencify.authentication.config.AppProperties;
import com.agencify.authentication.config.OtpDefaultProperties;
import com.agencify.authentication.dto.SendNotificationDto;
import com.agencify.authentication.events.producers.CreateAgentEvent;
import com.agencify.authentication.events.producers.CreateSubscriberProducerEvent;
import com.agencify.authentication.exception.error.InvalidOtpException;
import com.agencify.authentication.exception.error.OTPException;
import com.agencify.authentication.exception.error.UserDoesNotExistsException;
import com.agencify.authentication.model.User;
import com.agencify.authentication.repository.UserRepository;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.*;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    @Mock
    private Keycloak keycloak;

    @Mock
    NotificationClient notificationClient;

    @Captor
    private ArgumentCaptor<SendNotificationDto> sendNotificationDtoCaptor;


    @Mock
    private EntitiesClient entitiesClient;

    @Mock
    CreateAgentEvent createAgentEvent;

    @Mock
    CreateSubscriberProducerEvent createSubscriberProducerEvent;

    @Mock
    UserRepository userRepository;

    @Mock
    private OtpDefaultProperties otpDefaultProperties;

    private final RealmResource realmResource = Mockito.mock(RealmResource.class);

    private final UsersResource usersResource = Mockito.mock(UsersResource.class);

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    AppProperties appProperties;

    @AfterAll
    static void tearDown() throws IOException {
      //  keycloackMock.shutdown();
    }



    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(userService, "username", "agencify");
        ReflectionTestUtils.setField(userService, "password", "admin");

        this.otpDefaultProperties.setOtp("1234");
        this.otpDefaultProperties.setPhoneNumber("1234567890");
    }


    @Test
    void verifyUserEmail() {
      User user = new User();
      user.setEmailAddress("jmn.mumo@gmail.com");

      Mockito.when(entitiesClient.verifyUser(user.getEmailAddress())).thenReturn(user);

      var results = userService.verifyUserEmail(user.getEmailAddress());

      Assertions.assertNotNull(results);

    }

    @Test
    void updateUser() throws UserDoesNotExistsException {
        Mockito.mock(UsersResource.class);
        User user = new User();
        user.setOtp(4578);
        user.setEmailAddress("email78345");
        user.setOrganizationId(123L);

        when(otpDefaultProperties.getOtp()).thenReturn("1234");
        when(otpDefaultProperties.getPhoneNumber()).thenReturn("12453463634");
        int rand = userService.generateOTP("Stringforkey");

        user.setRole("ROLE_AGENT");
        user.setPassword("12345678test");


        Mockito.mock(UserRepresentation.class);

        String email = "email78345".concat(String.valueOf(rand));
        user.setEmailAddress(email);
        when(appProperties.getAuthServerUrl()).thenReturn("https://adungu.agencify.insure/auth");
        when(appProperties.getRealm()).thenReturn("master");
        when(realmResource.users()).thenReturn(usersResource);
        when(entitiesClient.register(user)).thenReturn(user);
        Assertions.assertThrows(UserDoesNotExistsException.class,()->userService.updateUser(user,5677));


    }

    @Test
    void sendWelcomeEmailEvent() {

        // Set up the test data
        User user = new User();
        user.setEmailAddress("test@example.com");
        user.setPhoneNumber("123456789");
        user.setFirstName("John");
        user.setLastName("Doe");

        // Call the method under test

        userService.sendWelcomeEmailEvent(user);

        Mockito.verify(createAgentEvent).sendNotification(sendNotificationDtoCaptor.capture());

    }

    @Test
    void resetPasswordRequest() throws OTPException, UserDoesNotExistsException {

        Mockito.mock(UsersResource.class);
        User user = new User();
        user.setOtp(4578);
      //  user.setEmailAddress("email78345");
        user.setOrganizationId(123L);


        user.setRole("ROLE_AGENT4");
        user.setPassword("12345678test");


        Mockito.mock(UserRepresentation.class);

        String email = "jmn.mumo@gmail.com";
        user.setEmailAddress(email);
        when(appProperties.getAuthServerUrl()).thenReturn("https://adungu.agencify.insure/auth");
        when(appProperties.getRealm()).thenReturn("master");
        when(realmResource.users()).thenReturn(usersResource);
        Assertions.assertThrows(UserDoesNotExistsException.class,()->userService.resetPasswordRequest(email));

    }

    @Test
    void verifyOtp() {
        User user = new User();
        user.setOtp(4578);
        user.setEmailAddress("email78345");
        user.setOrganizationId(123L);
        user.setOtp(8975);

        int Otp = 8975;

     Mockito.when(userRepository.findTopByEmailAddressOrderByIdDesc(user.getEmailAddress())).thenReturn(user);

     var results = userService.verifyOtp(user.getEmailAddress(), user.getOtp());
      Assertions.assertTrue(results);
    }

    @Test
    void resetPassword() throws UserDoesNotExistsException {
        Mockito.mock(UsersResource.class);
        User user = new User();
        user.setOtp(4578);
        user.setEmailAddress("email78345");
        user.setOrganizationId(123L);
        when(otpDefaultProperties.getOtp()).thenReturn("1234");
        when(otpDefaultProperties.getPhoneNumber()).thenReturn("1353553234");
        int rand = userService.generateOTP("Stringforkey");

        user.setRole("ROLE_AGENT");
        user.setPassword("12345678test");


        Mockito.mock(UserRepresentation.class);

        String email = "email78345".concat(String.valueOf(rand));
        user.setEmailAddress(email);
        when(appProperties.getAuthServerUrl()).thenReturn("https://adungu.agencify.insure/auth");
        when(appProperties.getRealm()).thenReturn("master");
        when(realmResource.users()).thenReturn(usersResource);
        when(entitiesClient.register(user)).thenReturn(user);


        Assertions.assertThrows(UserDoesNotExistsException.class,()->userService.resetPassword(user));


    }





    @Test
    void generateOTP_returnsDefaultOTP_whenPhoneNumberMatches() {
        String phoneNumber = "1234567890";
        String defaultPhoneNumber = "1234567890";
        String defaultOTP = "1234";

        when(otpDefaultProperties.getPhoneNumber()).thenReturn(defaultPhoneNumber);
        when(otpDefaultProperties.getOtp()).thenReturn(defaultOTP);

        int otp = userService.generateOTP(phoneNumber);

        assertTrue(otp == Integer.parseInt(defaultOTP));
    }

    @Test
    void generateOTP_returnsRandomOTP_whenPhoneNumberDoesNotMatch() {
        String phoneNumber = "1234567890";
        String defaultPhoneNumber = "0987654321";

        when(otpDefaultProperties.getPhoneNumber()).thenReturn(defaultPhoneNumber);

        int otp = userService.generateOTP(phoneNumber);

        assertTrue(otp >= 1000 && otp <= 9999);
    }


}
