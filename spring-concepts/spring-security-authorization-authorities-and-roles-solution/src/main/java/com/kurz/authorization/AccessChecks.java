package com.kurz.authorization;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authorization.AuthorityAuthorizationManager;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Runs role-based and authority-based authorization checks against a user's granted authorities,
 * without needing a servlet filter chain or a web request — AuthorityAuthorizationManager evaluates
 * an Authentication's authorities directly.
 */
public class AccessChecks {

    public boolean isGrantedByRole(UserDetails user, String role) {
        var authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        return AuthorityAuthorizationManager.<Object>hasRole(role)
            .authorize(() -> authentication, new Object())
            .isGranted();
    }

    public boolean isGrantedByAuthority(UserDetails user, String authority) {
        var authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        return AuthorityAuthorizationManager.<Object>hasAuthority(authority)
            .authorize(() -> authentication, new Object())
            .isGranted();
    }
}
