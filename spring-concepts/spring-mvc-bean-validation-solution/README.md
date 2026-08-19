# Spring MVC Bean Validation - Solution

## Overview

This is the official solution for the Spring MVC Bean Validation lab. It
demonstrates declaring Bean Validation constraints on a command object and
wiring `@Valid`/`Errors` into a Spring MVC handler.

## Key Concepts

### Declarative constraints on the domain class

`Order` carries its validation rules as annotations instead of hand-written
`if`/`then` checks:

```java
@NotBlank(message = "Name is required")
private String name;

@CreditCardNumber(message = "Not a valid credit card number")
private String ccNumber;

@Pattern(regexp = "^(0[1-9]|1[0-2])([\\/])([1-9][0-9])$", message = "Must be formatted MM/YY")
private String ccExpiration;

@Digits(integer = 3, fraction = 0, message = "Invalid CVV")
private String ccCVV;
```

`@CreditCardNumber` (from `org.hibernate.validator.constraints`) runs a Luhn
check — it catches typos and malformed numbers, not whether the card is
actually chargeable. `@Pattern` covers a fixed shape (`MM/YY`) that has no
purpose-built annotation. Every constraint's `message` is what a caller sees
when that specific rule fails.

### `@Valid` + `Errors` in the handler

```java
@PostMapping("/orders")
public String processOrder(@Valid Order order, Errors errors) {
    if (errors.hasErrors()) {
        return "orderForm";
    }
    return "redirect:/orders/current";
}
```

`@Valid` tells Spring MVC to run validation right after binding the submitted
form data, before the method body executes. The outcome lands in `errors` —
but only because `errors` is declared as the very next parameter.

## Implementation Details

### `OrderControllerTest`

Each test posts a form with exactly one broken field (blank name/street, an
invalid card number, a malformed expiration, a non-numeric CVV) and asserts
the controller returns to `"orderForm"` with a field error attached to that
field. A fully valid submission asserts a redirect instead.

### The `Errors`/`BindingResult` adjacency requirement (`TODO-05`)

`processOrderMisordered()` is identical to `processOrder()` except for one
extra `String note` parameter wedged between `@Valid Order order` and
`Errors errors`:

```java
@PostMapping("/orders/misordered")
public String processOrderMisordered(@Valid Order order,
                                      @RequestParam(required = false) String note,
                                      Errors errors) {
    if (errors.hasErrors()) {
        return "orderForm";
    }
    return "redirect:/orders/current";
}
```

Spring MVC resolves `Errors`/`BindingResult` **positionally** — it only looks
at the parameter immediately following the validated argument. Here that
parameter is `note`, a `String`, not an `Errors`/`BindingResult` type. Spring
notices there's no adjacent binding-result parameter and, when validation
fails, throws a `BindException` while resolving the `order` argument —
**before `processOrderMisordered()`'s body ever runs**. The request never
reaches `errors.hasErrors()`; it fails with a `400 Bad Request` instead of
redisplaying the form.

This is why the article calls the requirement a trap rather than a checked
rule: nothing about the method signature fails to compile, and the app starts
up fine. The mismatch only surfaces the first time an invalid request hits
that specific endpoint.

## Trade-offs

- Declarative validation reads better than hand-written checks, but a rule
  spanning multiple fields (e.g., "if payment type is credit card, `ccNumber`
  is required") needs a class-level constraint or a custom `Validator` —
  Bean Validation isn't a full substitute for business-rule validation.
- The `Errors`/`BindingResult` parameter must come immediately after the
  `@Valid` argument it corresponds to. Reordering doesn't fail at compile
  time or at startup — it fails the first time a request actually exercises
  the broken ordering, as `processOrderMisordered()` shows.
- `spring-boot-starter-validation` must be added explicitly — since Spring
  Boot 2.3, it's no longer pulled in transitively by
  `spring-boot-starter-web`.

## Summary

- Bean Validation annotations declare constraints once, on the domain class,
  instead of scattering checks through handler methods.
- `@Valid` triggers validation at form-binding time; `Errors` (immediately
  after) carries the outcome.
- Misordering the `Errors` parameter compiles cleanly and starts up fine, but
  breaks error capture — and the request — at the first invalid submission.
