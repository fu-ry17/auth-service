//package com.agencify.authentication.auth;
//
//import org.springframework.security.core.Authentication;
//import org.springframework.security.oauth2.jwt.Jwt;
//import org.springframework.stereotype.Component;
//import org.springframework.util.Assert;
//
//import java.util.HashMap;
//import java.util.Map;
//
///**
// *
// */
//@Component
//public class TokenUtils {
//
//    private Map<String, Object> claims = new HashMap<String, Object>();
//
//    private Authentication authentication = null;
//
//    public void init(Authentication authentication) {
//        this.authentication = authentication;
//        var kp = (Jwt) authentication.getPrincipal();
//        claims = kp.getClaims();
//    }
//
//    public Long getOrganizationId() {
//        Assert.notNull(authentication, "Organization Id must not be null");
//        return Long.parseLong(getClaimByName("organization_id").toString());
//    }
//
//    public String getUsername() {
//        Assert.notNull(authentication, "Username must not be null");
//        return (String) getClaimByName("name");
//    }
//
//    public String getLicenceNo() {
//        Assert.notNull(authentication, "Licence No must not be null");
//        return (String) getClaimByName("licence_no");
//    }
//
//    public String getPhoneNumber() {
//        Assert.notNull(authentication, "Phone number must not be null");
//        return (String) getClaimByName("phone_number");
//    }
//
//    public Object getClaimByName(String name) {
//        Assert.notNull(authentication, "Claim Name must not be null");
//        return claims.get(name);
//    }
//
//    public String getSub() {
//        Assert.notNull(authentication, "Claim Id must not be null");
//        return (String) claims.get("sub");
//    }
//
//}
