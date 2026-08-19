package com.kurz.mvcrequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/signup")
public class SignupController {

    @GetMapping
    public String showSignupForm(Model model) {
        // TODO-00: Add a new SignupForm to the model under the attribute name
        // "signupForm", then return the view name "signup-form".
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    // TODO-03 (optional): Add an explicit @ModelAttribute("signupForm") annotation to
    // the "form" parameter below. Implicit binding (no annotation) already works today,
    // but Spring's own docs recommend making it explicit for GraalVM native-image AOT
    // hint generation, which can't infer implicit @ModelAttribute binding.
    @PostMapping
    public String processSignup(SignupForm form, RedirectAttributes redirectAttributes) {
        // TODO-01: Add form.getUsername() to redirectAttributes as a flash attribute
        // named "username".

        // TODO-02: Return the redirect view name that sends the browser to
        // /signup/success. Remember: Spring only issues an HTTP redirect when the
        // returned view name starts with the "redirect:" prefix — anything else is
        // resolved as a template name instead.
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @GetMapping("/success")
    public String showSuccessPage() {
        return "signup-success";
    }
}
