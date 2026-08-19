package com.kurz.password;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UserRegistry")
class UserRegistryTest {

    @Test
    @DisplayName("should authenticate a registered user with the correct raw password")
    void shouldAuthenticateWithCorrectRawPassword() {
        var registry = new UserRegistry(new BCryptPasswordEncoder());
        registry.register("alice", "s3cret!");

        assertTrue(registry.authenticate("alice", "s3cret!"));
    }

    @Test
    @DisplayName("should reject authentication with a wrong raw password")
    void shouldRejectAuthenticationWithWrongRawPassword() {
        var registry = new UserRegistry(new BCryptPasswordEncoder());
        registry.register("alice", "s3cret!");

        assertFalse(registry.authenticate("alice", "wrong-password"));
    }

    @Test
    @DisplayName("should reject authentication for a username that was never registered")
    void shouldRejectAuthenticationForUnknownUser() {
        var registry = new UserRegistry(new BCryptPasswordEncoder());

        assertFalse(registry.authenticate("ghost", "anything"));
    }

    @Test
    @DisplayName("should never store the raw password as the encoded value")
    void shouldNeverStoreTheRawPasswordItself() {
        var registry = new UserRegistry(new BCryptPasswordEncoder());
        registry.register("alice", "s3cret!");

        assertNotEquals("s3cret!", registry.encodedPasswordOf("alice").orElseThrow());
    }

    @Test
    @DisplayName("should encode the same raw password differently on every registration, yet both encodings still match it")
    void shouldEncodeSameRawPasswordDifferentlyButBothStillMatch() {
        var registry = new UserRegistry(new BCryptPasswordEncoder());

        registry.register("alice", "s3cret!");
        String firstEncoded = registry.encodedPasswordOf("alice").orElseThrow();

        registry.register("alice", "s3cret!");
        String secondEncoded = registry.encodedPasswordOf("alice").orElseThrow();

        assertNotEquals(firstEncoded, secondEncoded,
            "BCrypt bakes a random salt into every encoded value, so naive .equals() between two "
                + "encodings of the same raw password is always false");
        assertTrue(registry.authenticate("alice", "s3cret!"),
            "matches() must still validate the raw password against whichever encoded value is on file");
    }

    @Test
    @DisplayName("plain BCryptPasswordEncoder.encode() called twice on the same raw password never produces equal strings")
    void shouldProveEncodeIsNonDeterministicAtTheEncoderLevel() {
        var encoder = new BCryptPasswordEncoder();

        String first = encoder.encode("s3cret!");
        String second = encoder.encode("s3cret!");

        assertNotEquals(first, second);
        assertTrue(encoder.matches("s3cret!", first));
        assertTrue(encoder.matches("s3cret!", second));
    }

    @Test
    @DisplayName("(optional) usingDelegatingEncoder() should produce a bcrypt-prefixed encoded password")
    void shouldUseDelegatingEncoderWithBcryptPrefix() {
        var registry = UserRegistry.usingDelegatingEncoder();
        registry.register("alice", "s3cret!");

        String encoded = registry.encodedPasswordOf("alice").orElseThrow();

        assertTrue(encoded.startsWith("{bcrypt}"), "expected a {bcrypt}-prefixed value but got: " + encoded);
        assertTrue(registry.authenticate("alice", "s3cret!"));
    }
}
