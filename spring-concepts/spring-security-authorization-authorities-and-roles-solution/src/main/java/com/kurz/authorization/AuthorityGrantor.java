package com.kurz.authorization;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Builds users carrying different flavors of the "admin" privilege, to explore the ROLE_
 * prefix rule that hasRole()/hasAuthority() apply asymmetrically.
 */
public class AuthorityGrantor {

    public UserDetails grantAdminRole(String username, String password) {
        return User.withUsername(username)
            .password(password)
            .authorities("ROLE_ADMIN")
            .build();
    }

    public UserDetails grantAdminAuthorityWithoutRolePrefix(String username, String password) {
        return User.withUsername(username)
            .password(password)
            .authorities("ADMIN")
            .build();
    }

    public UserDetails grantAdminRoleUsingRolesBuilder(String username, String password) {
        return User.withUsername(username)
            .password(password)
            .roles("ADMIN")
            .build();
    }
}
