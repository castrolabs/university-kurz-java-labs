package com.kurz.authorization;

import org.springframework.security.authorization.AuthorityAuthorizationManager;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

/**
 * Runs role-based and authority-based authorization checks against a user's granted authorities,
 * without needing a servlet filter chain or a web request — AuthorityAuthorizationManager evaluates
 * an Authentication's authorities directly.
 */
public class AccessChecks {

    public boolean isGrantedByRole(UserDetails user, String role) {
        // TODO-02: Build an Authentication from user (e.g. new UsernamePasswordAuthenticationToken(
        // user, null, user.getAuthorities())), then check it with
        // AuthorityAuthorizationManager.hasRole(role).authorize(...).isGranted().
        // Remember: hasRole(role) checks for the authority "ROLE_" + role internally.

        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public boolean isGrantedByAuthority(UserDetails user, String authority) {
        // TODO-03: Same idea as isGrantedByRole(), but using
        // AuthorityAuthorizationManager.hasAuthority(authority) — this checks for the literal
        // authority string, with no ROLE_ prefixing.

        throw new UnsupportedOperationException("Not implemented yet.");
    }
}
