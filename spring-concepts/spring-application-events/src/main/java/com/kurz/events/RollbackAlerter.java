package com.kurz.events;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Component;

/** Records every title whose publishing transaction was rolled back. */
@Component
public class RollbackAlerter {

    private final List<String> alerts = new CopyOnWriteArrayList<>();

    // TODO-03: Record a title only when its transaction ROLLS BACK.
    // Hint: TransactionPhase.
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
