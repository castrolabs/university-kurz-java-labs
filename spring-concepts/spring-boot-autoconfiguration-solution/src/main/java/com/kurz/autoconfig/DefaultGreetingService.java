package com.kurz.autoconfig;

public class DefaultGreetingService implements GreetingService {

    @Override
    public String greet() {
        return "Hello from the default greeting service!";
    }
}
