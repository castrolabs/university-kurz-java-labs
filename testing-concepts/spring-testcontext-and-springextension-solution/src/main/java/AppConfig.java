import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Plain Java Spring configuration - no component scanning, no Spring Boot. The
 * static counter exists purely to make context creation observable: every time
 * Spring invokes this {@code @Bean} method to build a MessageService, the counter
 * goes up. Because the TestContext framework caches and reuses a context across
 * test methods (and classes) with an identical configuration, the counter should
 * stay at 1 for every test that shares this AppConfig - unless something (like
 * {@code @DirtiesContext}) forces a fresh context to be built.
 */
@Configuration
public class AppConfig {

    public static final AtomicInteger creationCount = new AtomicInteger(0);

    @Bean
    public MessageService messageService() {
        creationCount.incrementAndGet();
        return new MessageService();
    }
}
