package com.kurz.boottesting;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * A trivial controller with zero collaborators - nothing here needs mocking, which
 * is exactly what makes it a good fit for a fast {@code @WebMvcTest} slice instead of
 * a full {@code @SpringBootTest}.
 */
@RestController
public class GreetingController {

    @GetMapping(value = "/greeting", produces = MediaType.TEXT_PLAIN_VALUE)
    public String greet() {
        return "Hello, Kurz!";
    }
}
