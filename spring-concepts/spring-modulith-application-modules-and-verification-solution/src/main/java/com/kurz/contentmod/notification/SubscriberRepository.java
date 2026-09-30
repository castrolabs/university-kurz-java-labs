package com.kurz.contentmod.notification;

import java.util.List;

import org.springframework.stereotype.Repository;

/** Package-private: nothing outside the notification package can even import it. */
@Repository
class SubscriberRepository {

    List<String> findAll() {
        return List.of("ada@example.com", "linus@example.com");
    }
}
