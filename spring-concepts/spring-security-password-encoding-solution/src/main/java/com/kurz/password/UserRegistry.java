package com.kurz.password;

import org.springframework.security.crypto.factory.PasswordEncoderFactories;
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
        encodedPasswordsByUsername.put(username, passwordEncoder.encode(rawPassword));
    }

    public boolean authenticate(String username, String rawPassword) {
        String encodedPassword = encodedPasswordsByUsername.get(username);
        if (encodedPassword == null) {
            return false;
        }

        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    public Optional<String> encodedPasswordOf(String username) {
        return Optional.ofNullable(encodedPasswordsByUsername.get(username));
    }

    public static UserRegistry usingDelegatingEncoder() {
        return new UserRegistry(PasswordEncoderFactories.createDelegatingPasswordEncoder());
    }
}
