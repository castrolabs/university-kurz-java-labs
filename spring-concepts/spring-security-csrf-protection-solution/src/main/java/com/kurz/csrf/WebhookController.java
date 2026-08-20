package com.kurz.csrf;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * A machine-to-machine callback endpoint. It is state-changing but is never called by a browser
 * carrying a session cookie, so it is a legitimate candidate for exclusion from CSRF protection —
 * see SecurityConfig's TODO-01.
 */
@RestController
public class WebhookController {

    @PostMapping("/webhooks/notify")
    public String handleNotification() {
        return "received";
    }
}
