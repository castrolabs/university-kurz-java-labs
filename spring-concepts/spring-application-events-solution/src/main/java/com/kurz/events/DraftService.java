package com.kurz.events;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/** Drafts are not persisted, so there is no transaction around this method. */
@Service
public class DraftService {

    private final ApplicationEventPublisher events;

    public DraftService(ApplicationEventPublisher events) {
        this.events = events;
    }

    public void saveDraft(String title) {
        events.publishEvent(new DraftSaved(title));
    }
}
