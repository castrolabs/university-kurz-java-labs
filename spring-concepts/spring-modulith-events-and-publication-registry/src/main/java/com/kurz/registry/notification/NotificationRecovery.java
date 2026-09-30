package com.kurz.registry.notification;

import org.springframework.modulith.events.FailedEventPublications;
import org.springframework.modulith.events.ResubmissionOptions;
import org.springframework.stereotype.Service;

/** What an ops endpoint or a @Scheduled job would call once the gateway is back. */
@Service
public class NotificationRecovery {

    private final FailedEventPublications failed;

    NotificationRecovery(FailedEventPublications failed) {
        this.failed = failed;
    }

    // TODO-02: Resubmit every FAILED publication so its listener runs again.
    // Hint: FailedEventPublications and ResubmissionOptions.defaults().
    public void retryFailed() {
        throw new UnsupportedOperationException("Not implemented yet.");
    }
}
