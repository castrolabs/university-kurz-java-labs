package com.kurz.events;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/** Records every title whose publishing transaction was rolled back. */
@Component
public class RollbackAlerter {

    private final List<String> alerts = new CopyOnWriteArrayList<>();

    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    public void on(ContentPublished event) {
        alerts.add(event.title());
    }

    public List<String> alerts() {
        return List.copyOf(alerts);
    }

    public void clear() {
        alerts.clear();
    }
}
