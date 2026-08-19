# Spring MVC Request Handling - Solution

## Overview

This is the official solution for the Spring MVC Request Handling lab. It shows the classic
"show form / process form / redirect" flow that predates JSON APIs but still underlies a lot of
server-rendered Spring MVC applications.

## Key Concepts

### `Model` is the handoff to the view

```java
@GetMapping
public String showSignupForm(Model model) {
    model.addAttribute("signupForm", new SignupForm());
    return "signup-form";
}
```

Attributes added to `Model` become request attributes a view template can read. The returned
string is a logical view name, not a path — a configured `ViewResolver` maps it to an actual
template.

### Implicit command-object binding

```java
@PostMapping
public String processSignup(SignupForm form, RedirectAttributes redirectAttributes) { ... }
```

`SignupForm` is not a "simple type" (`String`, `int`, ...) and no other Spring MVC argument
resolver claims it, so Spring treats the parameter as an implicit `@ModelAttribute` and runs its
data-binding machinery against the submitted request parameters — no `@ModelAttribute`
annotation and no manual `request.getParameter(...)` calls required. This lab adds the
annotation explicitly (TODO-03) because Spring's own docs recommend it for GraalVM native-image
builds, where implicit binding can't be inferred for AOT reflection hints — but the mechanism
works identically either way.

### `redirect:` and flash attributes

```java
redirectAttributes.addFlashAttribute("username", form.getUsername());
return "redirect:/signup/success";
```

A view name starting with `redirect:` tells Spring MVC to issue an HTTP redirect instead of
resolving a template — the browser makes a fresh `GET` request to the target path. Because a
redirect starts an entirely new request, the original `Model` doesn't survive it; `RedirectAttributes.addFlashAttribute`
stashes a value in the session for exactly one request, which is how `GET /signup/success` could
read it back if it needed to.

## Implementation Details

`SignupForm` is a plain mutable class with a no-args constructor and setters — the shape Spring
MVC's data binder expects when populating an object from request parameters.

## Trade-offs and Best Practices

1. **Implicit binding is concise but non-obvious**: a reader has to know Spring's
   argument-resolution rule to see that `processSignup(SignupForm form, ...)` is binding request
   parameters at all — there's no annotation to grep for unless it's added explicitly.
2. **`redirect:` is easy to omit by accident**: returning `"/signup/success"` from `processSignup`
   (without the prefix) would make Spring try to resolve a *template* named `/signup/success`
   instead of redirecting — the bug looks like a missing/misnamed template rather than a missing
   prefix.
3. **Flash attributes only survive one redirect**: they're removed from the session as soon as
   they're read by the next request, so they're appropriate for one-shot messages like "signup
   successful" but not for longer-lived state.

## Summary

- `Model` carries data from a `@GetMapping` handler to its view.
- A non-simple-type `@PostMapping` parameter is implicitly bound from form data — `@ModelAttribute`
  makes that explicit, which matters for GraalVM AOT builds.
- `redirect:`-prefixed view names issue real HTTP redirects; `RedirectAttributes` carries data
  across that redirect via flash attributes.
