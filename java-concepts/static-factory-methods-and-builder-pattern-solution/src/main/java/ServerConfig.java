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

    public static ServerConfig localhost(int port) {
        return new Builder("localhost", port).build();
    }

    public static synchronized ServerConfig defaultConfig() {
        if (cachedDefaultConfig == null) {
            cachedDefaultConfig = new Builder("localhost", 8080).build();
        }
        return cachedDefaultConfig;
    }

    public static ServerConfig withPort(ServerConfig base, int newPort) {
        Builder builder = new Builder(base.host(), newPort)
                .connectionTimeoutSeconds(base.connectionTimeoutSeconds());

        if (base.useTls()) {
            builder.useTls();
        }
        if (base.tlsCertificatePath() != null) {
            builder.tlsCertificatePath(base.tlsCertificatePath());
        }
        if (base.maxConnections() != null) {
            builder.maxConnections(base.maxConnections());
        }
        if (base.unlimited()) {
            builder.unlimited();
        }
        return builder.build();
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

        public Builder useTls() {
            this.useTls = true;
            return this;
        }

        public Builder tlsCertificatePath(String tlsCertificatePath) {
            this.tlsCertificatePath = tlsCertificatePath;
            return this;
        }

        public Builder maxConnections(int maxConnections) {
            this.maxConnections = maxConnections;
            return this;
        }

        public Builder unlimited() {
            this.unlimited = true;
            return this;
        }

        public Builder connectionTimeoutSeconds(int connectionTimeoutSeconds) {
            this.connectionTimeoutSeconds = connectionTimeoutSeconds;
            return this;
        }

        public ServerConfig build() {
            if (host == null || host.isBlank()) {
                throw new IllegalStateException("host is required");
            }
            if (port < 1 || port > 65535) {
                throw new IllegalStateException("port must be between 1 and 65535, got " + port);
            }
            if (useTls && (tlsCertificatePath == null || tlsCertificatePath.isBlank())) {
                throw new IllegalStateException("tlsCertificatePath is required when useTls is enabled");
            }
            if (maxConnections != null && unlimited) {
                throw new IllegalStateException("maxConnections and unlimited are mutually exclusive");
            }
            return new ServerConfig(host, port, useTls, tlsCertificatePath, maxConnections, unlimited,
                    connectionTimeoutSeconds);
        }
    }
}
