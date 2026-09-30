package com.kurz.contentmod;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Every direct subpackage of this package is an application module. */
@SpringBootApplication
public class ContentModApplication {

    public static void main(String[] args) {
        SpringApplication.run(ContentModApplication.class, args);
    }
}
