package com.kurz.testingauth;

import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Grants ROLE_ADMIN to an arbitrary username by manually building the SecurityContext — see
 * CustomAdminSecurityContextFactory. Unlike @WithUserDetails, the username never has to exist in
 * the real UserDetailsService; the Authentication is fabricated directly, exactly like
 * @WithMockUser is under the hood.
 */
@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = CustomAdminSecurityContextFactory.class)
public @interface WithCustomAdmin {

    String username();
}
