package com.kurz.registry.notification;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Component;

/** Stands in for an SMTP server that can go down. */
@Component
public class MailGateway {

    private volatile boolean down;
    private final List<String> delivered = new CopyOnWriteArrayList<>();

    void send(String subject) {
        if (down) {
            throw new IllegalStateException("Mail gateway is down");
        }
        delivered.add(subject);
    }

    public void setDown(boolean down) {
        this.down = down;
    }

    public List<String> delivered() {
        return List.copyOf(delivered);
    }

    public void clear() {
        delivered.clear();
        down = false;
    }
}
