package com.agencify.authentication.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Entity
@Table(name = "keycloak_users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "keycloak-users-seq")
    @SequenceGenerator(name="keycloak-users-seq", sequenceName = "keycloak_users_seq", allocationSize = 1, initialValue = 100000)
    private Long id;

    private String emailAddress;

    private String firstName;

    private String lastName;

    private String imageUrl;

    private String password;

    private String keycloakId;

    @NotNull
    private String phoneNumber;

    private Long organizationId;

    private String licenceNo;

    private int otp;

    private String role = "agent";

}
