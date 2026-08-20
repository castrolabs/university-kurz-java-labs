package com.kurz.csrf;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * A small product API used to exercise SecurityConfig's CSRF rules. GET is safe and always
 * reachable; POST, PUT, and DELETE are state-changing and — with CSRF protection enabled, the
 * default — require a valid CSRF token.
 */
@RestController
public class ProductController {

    @GetMapping("/products")
    public String list() {
        return "[]";
    }

    @PostMapping("/products")
    public String create() {
        return "created";
    }

    @PutMapping("/products/{id}")
    public String update(@PathVariable("id") String id) {
        return "updated " + id;
    }

    @DeleteMapping("/products/{id}")
    public String delete(@PathVariable("id") String id) {
        return "deleted " + id;
    }
}
