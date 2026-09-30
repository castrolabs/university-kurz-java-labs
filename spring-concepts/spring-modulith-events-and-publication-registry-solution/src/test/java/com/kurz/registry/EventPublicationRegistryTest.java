package com.kurz.registry;

import java.time.Duration;
import java.util.List;

import com.kurz.registry.notification.MailGateway;
import com.kurz.registry.notification.NotificationRecovery;
import com.kurz.registry.publishing.PublishingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.simple.JdbcClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

/**
 * Not @Transactional on purpose: the registry only does its job when the
 * publishing transaction really commits (or really rolls back).
 */
@SpringBootTest
class EventPublicationRegistryTest {

    @Autowired PublishingService publishing;
    @Autowired MailGateway mail;
    @Autowired NotificationRecovery recovery;
    @Autowired JdbcClient jdbc;

    @BeforeEach
    void reset() {
        jdbc.sql("DELETE FROM event_publication").update();
        jdbc.sql("DELETE FROM content").update();
        mail.clear();
    }

    List<String> statuses() {
        return jdbc.sql("SELECT status FROM event_publication").query(String.class).list();
    }

    long contentRows() {
        return jdbc.sql("SELECT COUNT(*) FROM content").query(Long.class).single();
    }

    @Test
    @DisplayName("TODO-00/01: a published event is delivered and its publication marked COMPLETED")
    void deliversAndCompletes() {
        publishing.publish("Modular monoliths");

        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            assertThat(mail.delivered()).containsExactly("New content: Modular monoliths");
            assertThat(statuses()).containsExactly("COMPLETED");
        });
    }

    @Test
    @DisplayName("TODO-01: a failing listener does not undo the publication, the registry keeps it as FAILED")
    void failingListenerKeepsPublication() {
        mail.setDown(true);

        publishing.publish("Written during an outage");

        assertThat(contentRows()).isEqualTo(1);
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() ->
                assertThat(statuses()).containsExactly("FAILED"));
        assertThat(mail.delivered()).isEmpty();
    }

    @Test
    @DisplayName("TODO-02: resubmitting failed publications delivers them once the gateway is back")
    void resubmitDelivers() {
        mail.setDown(true);
        publishing.publish("Delivered late");
        await().atMost(Duration.ofSeconds(5)).until(() -> statuses().equals(List.of("FAILED")));

        mail.setDown(false);
        recovery.retryFailed();

        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            assertThat(mail.delivered()).containsExactly("New content: Delivered late");
            assertThat(statuses()).containsExactly("COMPLETED");
        });
    }

    @Test
    @DisplayName("TODO-00: a rolled-back transaction leaves neither content nor a publication behind")
    void rollbackLeavesNothing() {
        assertThatThrownBy(() -> publishing.publish("Cheap spam offer"))
                .isInstanceOf(IllegalArgumentException.class);

        assertThat(contentRows()).isZero();
        assertThat(statuses()).isEmpty();
        assertThat(mail.delivered()).isEmpty();
    }
}
