import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ServerConfig")
class ServerConfigTest {

    @Test
    @DisplayName("should build a config with required fields and every optional field at its default")
    void shouldBuildConfigWithDefaults() {
        ServerConfig config = new ServerConfig.Builder("db.internal", 5432).build();

        assertEquals("db.internal", config.host());
        assertEquals(5432, config.port());
        assertFalse(config.useTls());
        assertNull(config.tlsCertificatePath());
        assertNull(config.maxConnections());
        assertFalse(config.unlimited());
        assertEquals(30, config.connectionTimeoutSeconds());
    }

    @Test
    @DisplayName("should apply every fluent optional setter")
    void shouldApplyFluentOptionalSetters() {
        ServerConfig config = new ServerConfig.Builder("db.internal", 5432)
                .useTls()
                .tlsCertificatePath("/etc/certs/db.pem")
                .connectionTimeoutSeconds(60)
                .maxConnections(50)
                .build();

        assertTrue(config.useTls());
        assertEquals("/etc/certs/db.pem", config.tlsCertificatePath());
        assertEquals(60, config.connectionTimeoutSeconds());
        assertEquals(50, config.maxConnections());
    }

    @Test
    @DisplayName("setters should not validate anything — only build() should throw")
    void settersShouldNeverValidateOnlyBuildShould() {
        ServerConfig.Builder builder = new ServerConfig.Builder("localhost", 8080).unlimited();

        // Setting a conflicting field must not throw here — the invariant
        // is only checked once, at build() time.
        assertDoesNotThrow(() -> builder.maxConnections(10));

        // The conflict is real, so build() must be the one to catch it.
        assertThrows(IllegalStateException.class, builder::build);
    }

    @Test
    @DisplayName("should throw at build() when useTls is set without a certificate path")
    void shouldThrowWhenUseTlsWithoutCertificatePath() {
        ServerConfig.Builder builder = new ServerConfig.Builder("db.internal", 5432).useTls();

        assertThrows(IllegalStateException.class, builder::build);
    }

    @Test
    @DisplayName("should not throw regardless of the order tlsCertificatePath and useTls are set in")
    void shouldNotThrowRegardlessOfSetterOrder() {
        ServerConfig config = new ServerConfig.Builder("db.internal", 5432)
                .tlsCertificatePath("/etc/certs/db.pem")
                .useTls()
                .build();

        assertTrue(config.useTls());
        assertEquals("/etc/certs/db.pem", config.tlsCertificatePath());
    }

    @Test
    @DisplayName("should throw at build() when both maxConnections and unlimited are set")
    void shouldThrowWhenMaxConnectionsAndUnlimitedAreBothSet() {
        ServerConfig.Builder builder = new ServerConfig.Builder("db.internal", 5432)
                .maxConnections(100)
                .unlimited();

        assertThrows(IllegalStateException.class, builder::build);
    }

    @Test
    @DisplayName("should throw at build() for a null or blank host")
    void shouldThrowForNullOrBlankHost() {
        assertThrows(IllegalStateException.class, () -> new ServerConfig.Builder(null, 8080).build());
        assertThrows(IllegalStateException.class, () -> new ServerConfig.Builder("   ", 8080).build());
    }

    @Test
    @DisplayName("should throw at build() for an out-of-range port")
    void shouldThrowForOutOfRangePort() {
        assertThrows(IllegalStateException.class, () -> new ServerConfig.Builder("localhost", 0).build());
        assertThrows(IllegalStateException.class, () -> new ServerConfig.Builder("localhost", 65536).build());
    }

    @Test
    @DisplayName("should accept the boundary port values 1 and 65535")
    void shouldAcceptBoundaryPortValues() {
        assertDoesNotThrow(() -> new ServerConfig.Builder("localhost", 1).build());
        assertDoesNotThrow(() -> new ServerConfig.Builder("localhost", 65535).build());
    }

    @Test
    @DisplayName("localhost(port) should create a config with host localhost and the given port")
    void shouldCreateLocalhostConfig() {
        ServerConfig config = ServerConfig.localhost(9000);

        assertEquals("localhost", config.host());
        assertEquals(9000, config.port());
        assertFalse(config.useTls());
    }

    @Test
    @DisplayName("defaultConfig() should return the exact same cached instance on every call")
    void shouldReturnSameCachedInstanceFromDefaultConfig() {
        ServerConfig first = ServerConfig.defaultConfig();
        ServerConfig second = ServerConfig.defaultConfig();

        assertSame(first, second);
        assertEquals("localhost", first.host());
        assertEquals(8080, first.port());
    }

    @Test
    @DisplayName("withPort(base, newPort) should copy every field except port (bonus)")
    void shouldCreateCopyWithDifferentPort() {
        ServerConfig base = new ServerConfig.Builder("db.internal", 5432)
                .useTls()
                .tlsCertificatePath("/etc/certs/db.pem")
                .connectionTimeoutSeconds(45)
                .build();

        ServerConfig copy = ServerConfig.withPort(base, 5433);

        assertEquals(5433, copy.port());
        assertEquals("db.internal", copy.host());
        assertTrue(copy.useTls());
        assertEquals("/etc/certs/db.pem", copy.tlsCertificatePath());
        assertEquals(45, copy.connectionTimeoutSeconds());
        assertEquals(5432, base.port(), "the original config must be unchanged");
    }

    @Test
    @DisplayName("withPort(base, newPort) should work when base has no maxConnections set (bonus)")
    void shouldCreateCopyWhenMaxConnectionsIsUnset() {
        ServerConfig base = new ServerConfig.Builder("db.internal", 5432).build();

        ServerConfig copy = ServerConfig.withPort(base, 5433);

        assertNull(copy.maxConnections());
        assertEquals(5433, copy.port());
    }
}
