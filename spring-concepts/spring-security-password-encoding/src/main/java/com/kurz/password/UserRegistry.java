package com.kurz.password;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Registers users by encoding their raw password, then authenticates them later
 * without ever storing (or comparing) the raw password itself.
 */
public class UserRegistry {

    private final PasswordEncoder passwordEncoder;
    private final Map<String, String> encodedPasswordsByUsername = new HashMap<>();

    public UserRegistry(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public void register(String username, String rawPassword) {
        // TODO-00: Encode rawPassword with passwordEncoder and store the encoded value keyed by
        // username in encodedPasswordsByUsername. Never store the raw password itself.

        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public boolean authenticate(String username, String rawPassword) {
        // TODO-01: Look up the encoded password stored for username.
        // TODO-02: Return false when no user was ever registered under that username.
        // TODO-03: Otherwise, compare rawPassword against the encoded password using
        // passwordEncoder.matches(rawPassword, encodedPassword) — never == or .equals() against
        // the encoded value, since the same raw password encodes differently every time.

        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public Optional<String> encodedPasswordOf(String username) {
        // TODO-04: Return the encoded password stored for username, wrapped in an Optional, or
        // Optional.empty() when the username was never registered.

        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public static UserRegistry usingDelegatingEncoder() {
        // TODO-05 (optional): Return a UserRegistry backed by
        // PasswordEncoderFactories.createDelegatingPasswordEncoder() instead of a raw
        // BCryptPasswordEncoder — its encoded values are prefixed with "{bcrypt}", letting the
        // application support several algorithms at once.

        throw new UnsupportedOperationException("Not implemented yet.");
    }
}
