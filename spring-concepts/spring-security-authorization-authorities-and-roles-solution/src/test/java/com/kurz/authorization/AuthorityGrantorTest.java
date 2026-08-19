package com.kurz.authorization;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AuthorityGrantor")
class AuthorityGrantorTest {

    private final AuthorityGrantor grantor = new AuthorityGrantor();

    @Test
    @DisplayName("grantAdminRole() should grant the ROLE_-prefixed authority \"ROLE_ADMIN\"")
    void shouldGrantRoleAdminAuthorityWithPrefix() {
        var user = grantor.grantAdminRole("john", "password");

        assertTrue(user.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    @Test
    @DisplayName("grantAdminAuthorityWithoutRolePrefix() should grant the literal authority \"ADMIN\", with no ROLE_ prefix")
    void shouldGrantLiteralAdminAuthorityWithoutPrefix() {
        var user = grantor.grantAdminAuthorityWithoutRolePrefix("jane", "password");

        assertTrue(user.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN")));
        assertFalse(user.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    @Test
    @DisplayName("(optional) grantAdminRoleUsingRolesBuilder() should also grant \"ROLE_ADMIN\", via the .roles() builder method")
    void shouldGrantRoleAdminUsingRolesBuilder() {
        var user = grantor.grantAdminRoleUsingRolesBuilder("john", "password");

        assertTrue(user.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }
}
