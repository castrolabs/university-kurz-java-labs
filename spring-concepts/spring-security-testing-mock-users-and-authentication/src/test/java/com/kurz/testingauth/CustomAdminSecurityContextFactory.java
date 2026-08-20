package com.kurz.testingauth;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.util.List;

/**
 * Builds a SecurityContext by hand, the escape hatch for when the code under test needs a
 * particular Authentication shape that @WithMockUser/@WithUserDetails can't produce. This is
 * exactly the technique @WithUserDetails itself uses internally — the only difference is where the
 * Authentication's details come from.
 */
public class CustomAdminSecurityContextFactory implements WithSecurityContextFactory<WithCustomAdmin> {

    @Override
    public SecurityContext createSecurityContext(WithCustomAdmin withCustomAdmin) {
        // TODO-05: Create an empty SecurityContext with SecurityContextHolder.createEmptyContext(),
        // build an Authentication with UsernamePasswordAuthenticationToken.authenticated(
        // withCustomAdmin.username(), null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))),
        // set it on the context with context.setAuthentication(...), and return the context.
        //
        // Notice what this method never does: it never calls the real UserDetailsService, never
        // runs a PasswordEncoder, never consults an AuthenticationProvider. Whatever username you
        // pass in is granted ROLE_ADMIN unconditionally, even one that doesn't exist in
        // SecurityConfig's user store at all.

        throw new UnsupportedOperationException("Not implemented yet.");
    }
}
