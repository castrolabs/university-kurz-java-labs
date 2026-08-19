package com.kurz.validation;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class OrderController {

    @GetMapping("/orders")
    public String orderForm() {
        return "orderForm";
    }

    @PostMapping("/orders")
    public String processOrder(@Valid Order order, Errors errors) {
        // TODO-04: If errors.hasErrors() is true, redisplay the form by returning
        // "orderForm" again. Otherwise, the order is valid: return
        // "redirect:/orders/current".
        //
        // Note how "errors" is declared as the parameter immediately after the
        // @Valid-annotated "order" argument — Spring MVC resolves it positionally.

        throw new UnsupportedOperationException("Not implemented yet.");
    }

    // TODO-05 (optional): Add a second handler, processOrderMisordered(@Valid Order
    // order, String note, Errors errors), mapped to POST "/orders/misordered", with
    // an unrelated @org.springframework.web.bind.annotation.RequestParam(required =
    // false) String note parameter placed BETWEEN the @Valid argument and Errors.
    // Post an invalid order to it and observe how the response differs from
    // processOrder() above — this is why the article calls the adjacency requirement
    // a silent trap rather than a compile-time or startup-time failure.
}
