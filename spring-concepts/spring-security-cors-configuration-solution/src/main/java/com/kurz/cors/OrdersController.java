package com.kurz.cors;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * A plain REST endpoint used to exercise SecurityConfig's CORS rules. Nothing here is
 * cross-origin-aware itself — CORS is handled entirely by the SecurityFilterChain, ahead of this
 * controller ever running.
 */
@RestController
public class OrdersController {

    @GetMapping("/orders")
    public String list() {
        return "[]";
    }

    @PostMapping(path = "/orders", consumes = MediaType.APPLICATION_JSON_VALUE)
    public String create(@RequestBody(required = false) String body) {
        return "created";
    }
}
