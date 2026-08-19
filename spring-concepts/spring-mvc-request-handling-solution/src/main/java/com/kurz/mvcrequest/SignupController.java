package com.kurz.mvcrequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/signup")
public class SignupController {

    @GetMapping
    public String showSignupForm(Model model) {
        model.addAttribute("signupForm", new SignupForm());
        return "signup-form";
    }

    @PostMapping
    public String processSignup(@ModelAttribute("signupForm") SignupForm form,
                                 RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("username", form.getUsername());
        return "redirect:/signup/success";
    }

    @GetMapping("/success")
    public String showSuccessPage() {
        return "signup-success";
    }
}
