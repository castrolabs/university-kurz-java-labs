/**
 * An immutable server startup configuration.
 *
 * <p>Two required fields ({@code host}, {@code port}) and several optional
 * ones — some of which depend on each other or are mutually exclusive:
 *
 * <ul>
 *   <li>{@code tlsCertificatePath} only makes sense (and is only required)
 *       when {@code useTls} is true.</li>
 *   <li>{@code maxConnections} and {@code unlimited} cannot both be set —
 *       a config can't be both capped and uncapped.</li>
 * </ul>
 *
 * <p>Building this with telescoping constructor overloads for every
 * combination of optional fields would be unreadable at the call site;
 * {@link Builder} solves it instead.
 */
public record ServerConfig(
        String host,
        int port,
        boolean useTls,
        String tlsCertificatePath,
        Integer maxConnections,
        boolean unlimited,
        int connectionTimeoutSeconds) {

    private static ServerConfig cachedDefaultConfig;

    /**
     * A named static factory for the common case of a local development
     * server: host "localhost", the given port, every other field at its
     * default.
     */
    public static ServerConfig localhost(int port) {
        // TODO-02: Return new Builder("localhost", port).build().

        throw new UnsupportedOperationException("Not implemented yet.");
    }

    /**
     * Returns a shared default configuration (host "localhost", port 8080,
     * every other field at its default). Every call returns the exact same
     * instance — this method never allocates a second one.
     */
    public static ServerConfig defaultConfig() {
        // TODO-03: Build the default instance once (host "localhost", port
        // 8080) and cache it in the cachedDefaultConfig field above, so
        // every subsequent call returns that same instance instead of
        // building a new one.

        throw new UnsupportedOperationException("Not implemented yet.");
    }

    /**
     * Returns a copy of {@code base} with only the port changed — every
     * other field carries over unchanged. {@code base} itself is untouched,
     * since {@code ServerConfig} is immutable.
     */
    public static ServerConfig withPort(ServerConfig base, int newPort) {
        // TODO-04 (optional): Rebuild base's fields through a new Builder,
        // substituting newPort for base.port(). Remember maxConnections is
        // an Integer that may be null (meaning "not set") — only call
        // .maxConnections(...) on the new builder when base.maxConnections()
        // isn't null, otherwise skip it.

        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public static final class Builder {
        private final String host;
        private final int port;
        private boolean useTls = false;
        private String tlsCertificatePath;
        private Integer maxConnections;
        private boolean unlimited = false;
        private int connectionTimeoutSeconds = 30;

        public Builder(String host, int port) {
            this.host = host;
            this.port = port;
        }

        // TODO-01: Implement the five fluent setters below. Each one simply
        // assigns its field and returns `this` — no validation belongs
        // here. Validation happens once, in build().

        public Builder useTls() {
            throw new UnsupportedOperationException("Not implemented yet.");
        }

        public Builder tlsCertificatePath(String tlsCertificatePath) {
            throw new UnsupportedOperationException("Not implemented yet.");
        }

        public Builder maxConnections(int maxConnections) {
            throw new UnsupportedOperationException("Not implemented yet.");
        }

        public Builder unlimited() {
            throw new UnsupportedOperationException("Not implemented yet.");
        }

        public Builder connectionTimeoutSeconds(int connectionTimeoutSeconds) {
            throw new UnsupportedOperationException("Not implemented yet.");
        }

        /**
         * Validates every invariant and constructs the immutable
         * {@link ServerConfig}. This is the only place validation happens —
         * the setters above never validate anything as they're called.
         */
        public ServerConfig build() {
            // TODO-00: Validate, in order:
            //   1. host is not null and not blank -> IllegalStateException
            //   2. port is between 1 and 65535 (inclusive) -> IllegalStateException
            //   3. if useTls is true, tlsCertificatePath must be non-null and
            //      non-blank -> IllegalStateException
            //   4. maxConnections and unlimited cannot both be set (maxConnections
            //      != null AND unlimited == true) -> IllegalStateException
            // Give each thrown exception a message that names which
            // invariant failed. If every check passes, construct and return
            // a new ServerConfig from this builder's fields.

            throw new UnsupportedOperationException("Not implemented yet.");
        }
    }
}
