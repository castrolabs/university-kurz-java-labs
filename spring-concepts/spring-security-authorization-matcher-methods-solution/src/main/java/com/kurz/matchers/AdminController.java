package com.kurz.matchers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * A small admin API used to exercise SecurityConfig's authorization rules.
 */
@RestController
public class AdminController {

    @GetMapping("/admin/dashboard")
    public String dashboard() {
        return "Admin Dashboard";
    }

    @GetMapping("/admin/health")
    public String health() {
        return "OK";
    }

    @GetMapping("/reports")
    public String reports() {
        return "Reports";
    }
}
