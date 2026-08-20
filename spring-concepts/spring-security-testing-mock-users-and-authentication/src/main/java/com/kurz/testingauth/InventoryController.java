package com.kurz.testingauth;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * A small inventory API used to exercise SecurityConfig's authorization rules — and, in the test
 * suite, the different ways of establishing a principal for a test.
 */
@RestController
public class InventoryController {

    @GetMapping("/inventory/view")
    public String view() {
        return "Inventory";
    }

    @GetMapping("/inventory/admin")
    public String admin() {
        return "Admin Inventory";
    }
}
