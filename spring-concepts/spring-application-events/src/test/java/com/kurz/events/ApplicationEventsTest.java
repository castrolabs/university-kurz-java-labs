package com.kurz.events;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Deliberately NOT @Transactional: every call below must run in its own real
 * transaction, otherwise AFTER_COMMIT / AFTER_ROLLBACK would only fire when the
 * test method itself ends.
 */
@SpringBootTest
class ApplicationEventsTest {

    @Autowired ContentService contentService;
    @Autowired DraftService draftService;
    @Autowired SearchIndexer searchIndexer;
    @Autowired RollbackAlerter rollbackAlerter;
    @Autowired DraftNotifier draftNotifier;

    @BeforeEach
    void reset() {
        contentService.deleteAll();
        searchIndexer.clear();
        rollbackAlerter.clear();
        draftNotifier.clear();
    }

    @Test
    @DisplayName("TODO-00: publish() stores the row and returns its generated id")
    void publishStoresRow() {
        long id = contentService.publish("Modular monoliths");

        assertThat(id).isPositive();
        assertThat(contentService.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("TODO-01: a synchronous listener can veto the publication and roll it back")
    void validatorVetoesSpam() {
        assertThatThrownBy(() -> contentService.publish("Cheap spam offer"))
                .isInstanceOf(IllegalArgumentException.class);

        assertThat(contentService.count()).isZero();
        assertThat(searchIndexer.indexed()).isEmpty();
    }

    @Test
    @DisplayName("TODO-02: the search index only sees committed content")
    void indexerRunsAfterCommit() {
        contentService.publish("Committed post");

        assertThat(searchIndexer.indexed()).containsExactly("Committed post");
    }

    @Test
    @DisplayName("TODO-02: a rolled-back batch leaves no ghost entries in the search index")
    void indexerIgnoresRolledBackBatch() {
        assertThatThrownBy(() -> contentService.publishAll(List.of("Intro", "Part 2", "Intro")))
                .isInstanceOf(DataIntegrityViolationException.class);

        assertThat(contentService.count()).isZero();
        assertThat(searchIndexer.indexed()).isEmpty();
    }

    @Test
    @DisplayName("TODO-03: rollback alerts list exactly the events published before the failure")
    void alerterSeesRollback() {
        assertThatThrownBy(() -> contentService.publishAll(List.of("Intro", "Part 2", "Intro")))
                .isInstanceOf(DataIntegrityViolationException.class);

        assertThat(rollbackAlerter.alerts()).containsExactly("Intro", "Part 2");
    }

    @Test
    @DisplayName("TODO-03: a successful publication raises no rollback alert")
    void alerterQuietOnCommit() {
        contentService.publish("Happy path");

        assertThat(rollbackAlerter.alerts()).isEmpty();
    }

    @Test
    @DisplayName("TODO-04: a transactional listener still runs when the publisher has no transaction")
    void draftNotifierRunsWithoutTransaction() {
        draftService.saveDraft("Work in progress");

        assertThat(draftNotifier.notified()).containsExactly("Work in progress");
    }
}
