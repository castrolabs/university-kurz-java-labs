package com.kurz.events;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * A transactional listener is silently skipped when no transaction is active,
 * unless fallbackExecution = true tells Spring to run it immediately instead.
 */
@Component
public class DraftNotifier {

    private final List<String> notified = new CopyOnWriteArrayList<>();

    // TODO-04: DraftService publishes without any transaction, so this
    // listener is silently skipped today. Keep it transactional (drafts may
    // be persisted one day) but make it also run when no transaction exists.
    @TransactionalEventListener
    public void on(DraftSaved event) {
        notified.add(event.title());
    }

    public List<String> notified() {
        return List.copyOf(notified);
    }

    public void clear() {
        notified.clear();
    }
}
