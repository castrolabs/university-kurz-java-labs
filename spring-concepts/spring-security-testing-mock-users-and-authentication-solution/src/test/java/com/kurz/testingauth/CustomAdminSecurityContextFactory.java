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
        var context = SecurityContextHolder.createEmptyContext();

        var authentication = UsernamePasswordAuthenticationToken.authenticated(
            withCustomAdmin.username(), null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

        context.setAuthentication(authentication);
        return context;
    }
}
