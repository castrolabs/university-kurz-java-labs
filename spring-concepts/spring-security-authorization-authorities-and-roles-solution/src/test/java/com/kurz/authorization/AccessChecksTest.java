package com.kurz.authorization;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AccessChecks")
class AccessChecksTest {

    private final AuthorityGrantor grantor = new AuthorityGrantor();
    private final AccessChecks accessChecks = new AccessChecks();

    @Test
    @DisplayName("hasRole(\"ADMIN\") should grant access to a user holding \"ROLE_ADMIN\"")
    void shouldGrantAccessWhenCheckingRoleAgainstCorrectlyPrefixedAuthority() {
        var user = grantor.grantAdminRole("john", "password");

        assertTrue(accessChecks.isGrantedByRole(user, "ADMIN"));
    }

    @Test
    @DisplayName("hasRole(\"ADMIN\") should deny access to a user holding only the bare \"ADMIN\" authority (missing ROLE_ prefix)")
    void shouldDenyAccessWhenCheckingRoleAgainstUnprefixedAuthority() {
        var user = grantor.grantAdminAuthorityWithoutRolePrefix("jane", "password");

        assertFalse(accessChecks.isGrantedByRole(user, "ADMIN"));
    }

    @Test
    @DisplayName("hasAuthority(\"ADMIN\") should grant access to a user holding the literal \"ADMIN\" authority")
    void shouldGrantAccessWhenCheckingAuthorityAgainstUnprefixedAuthority() {
        var user = grantor.grantAdminAuthorityWithoutRolePrefix("jane", "password");

        assertTrue(accessChecks.isGrantedByAuthority(user, "ADMIN"));
    }

    @Test
    @DisplayName("hasAuthority(\"ADMIN\") should deny access to a user holding only \"ROLE_ADMIN\" (a different, prefixed string)")
    void shouldDenyAccessWhenCheckingAuthorityAgainstPrefixedRoleAuthority() {
        var user = grantor.grantAdminRole("john", "password");

        assertFalse(accessChecks.isGrantedByAuthority(user, "ADMIN"));
    }
}
