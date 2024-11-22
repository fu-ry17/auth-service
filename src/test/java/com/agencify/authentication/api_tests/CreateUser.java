package com.agencify.authentication.api_tests;

import static io.restassured.RestAssured.*;

import com.agencify.authentication.api_tests.utils.Constants;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import java.util.UUID;
import static org.hamcrest.Matchers.*;

public class CreateUser {

    private String accessToken;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        baseURI = Constants.BASE_URI;
        accessToken = getNewAccessToken();

    }

    private String getNewAccessToken() {
        Response tokenResponse =
                given()
                        .contentType(ContentType.URLENC)
                        .formParam("grant_type", "password")
                        .formParam("client_id", "web-client")
                        .formParam("client_secret", "62f8c3fb-0fba-4358-a8da-80b52dd1e32b")
                        .formParam("username", "eva.mutuku@agencify.insure")
                        .formParam("password", "123456789")
                        .formParam("scope", "email profile")
                        .post(Constants.AUTH_URI);

        return tokenResponse.jsonPath().getString("access_token");
    }

    private String generateUniqueEmail() {
        String uniqueId = UUID.randomUUID().toString().replace("-", "");
        return "user" + uniqueId + "@example.com";
    }


    @Test
    public void testPostCreateUser() {

        String uniqueEmail = generateUniqueEmail();

        // The request body
        String requestBody = "{"
                + "\"emailAddress\": \"" + uniqueEmail + "\","
                + "\"firstName\": \"QA\","
                + "\"lastName\": \"TESTING\","
                + "\"phoneNumber\": \"0796253447\","
                + "\"password\": \"123456789\""
                + "}";

        Response response =

                given()
                        .auth().oauth2(accessToken)
                        .contentType(ContentType.JSON)
                        .body(requestBody).  // Include the request body
                    when()
                        .post("/uaa/users/create").
                    then()
                        .statusCode(200)
                        .assertThat()
                        .body(equalTo("true"))
                        .extract().response();


        System.out.println("Response: " + response.asString());

    }
}
