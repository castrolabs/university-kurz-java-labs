package com.kurz.validation;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class OrderController {

    @GetMapping("/orders")
    public String orderForm() {
        return "orderForm";
    }

    @PostMapping("/orders")
    public String processOrder(@Valid Order order, Errors errors) {
        if (errors.hasErrors()) {
            return "orderForm";
        }
        return "redirect:/orders/current";
    }

    @PostMapping("/orders/misordered")
    public String processOrderMisordered(@Valid Order order,
                                          @RequestParam(required = false) String note,
                                          Errors errors) {
        if (errors.hasErrors()) {
            return "orderForm";
        }
        return "redirect:/orders/current";
    }
}
