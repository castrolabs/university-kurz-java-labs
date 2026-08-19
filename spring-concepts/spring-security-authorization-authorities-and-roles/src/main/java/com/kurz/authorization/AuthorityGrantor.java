package com.kurz.authorization;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Builds users carrying different flavors of the "admin" privilege, to explore the ROLE_
 * prefix rule that hasRole()/hasAuthority() apply asymmetrically.
 */
public class AuthorityGrantor {

    public UserDetails grantAdminRole(String username, String password) {
        // TODO-00: Build a UserDetails for username/password granting the authority "ROLE_ADMIN"
        // (the ROLE_-prefixed form) using User.withUsername(...).password(...).authorities(...).build().
        // hasRole("ADMIN") checks for exactly this string internally.

        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public UserDetails grantAdminAuthorityWithoutRolePrefix(String username, String password) {
        // TODO-01: Build a UserDetails for username/password granting the literal authority "ADMIN"
        // (no ROLE_ prefix) using .authorities(...). This is the common mistake: it satisfies
        // hasAuthority("ADMIN") but NOT hasRole("ADMIN"), since hasRole() looks for "ROLE_ADMIN".

        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public UserDetails grantAdminRoleUsingRolesBuilder(String username, String password) {
        // TODO-04 (optional): Build the same outcome as grantAdminRole(), but using the builder's
        // .roles("ADMIN") method instead of .authorities("ROLE_ADMIN") — Spring adds the ROLE_
        // prefix automatically, so passing an already-prefixed value here would throw at build time.

        throw new UnsupportedOperationException("Not implemented yet.");
    }
}
