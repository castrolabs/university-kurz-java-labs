package com.kurz.csrf;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes the current request's CSRF token as plain text. CsrfFilter (positioned ahead of this
 * controller in the chain) puts the CsrfToken for the request on a request attribute named
 * "_csrf" — anything downstream of the filter can read it from there.
 */
@RestController
public class CsrfTokenController {

    @GetMapping("/csrf")
    public String csrf(HttpServletRequest request) {
        return ((CsrfToken) request.getAttribute("_csrf")).getToken();
    }
}
