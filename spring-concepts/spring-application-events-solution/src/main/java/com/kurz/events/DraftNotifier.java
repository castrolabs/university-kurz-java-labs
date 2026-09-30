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

    @TransactionalEventListener(fallbackExecution = true)
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
