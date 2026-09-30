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

    public void retryFailed() {
        failed.resubmit(ResubmissionOptions.defaults());
    }
}
