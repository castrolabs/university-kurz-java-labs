# Spring MVC Bean Validation

## Goal

Declare Bean Validation rules on a domain class and wire them into a Spring MVC
handler with `@Valid` and `Errors`, so an invalid submission is rejected before
it ever reaches business logic.

## Prerequisites

- Basic Spring MVC (`@Controller`, `@GetMapping`/`@PostMapping`)
- Familiarity with annotations
- Basic HTTP form submission concepts

## Task

`Order` is a command object for a checkout form: a name, a street, a credit card
number, an expiration date, and a CVV. None of its fields are validated yet, so
`OrderController` currently has no way to reject bad input. You'll add Bean
Validation annotations to `Order` and complete the handler that inspects the
validation outcome.

Pay close attention to the order of the two parameters in
`processOrder(@Valid Order order, Errors errors)`. Spring MVC resolves `Errors`
positionally: it only captures validation errors for the argument that
**immediately precedes** it. Move something between them and the binding still
compiles — it just quietly stops working the way you'd expect.

## Instructions

Complete the following TODOs:

- TODO-00: Add `@NotBlank` (with a `message`) to `Order.name` and `Order.street`.
- TODO-01: Add `@CreditCardNumber` (with a `message`) to `Order.ccNumber`.
- TODO-02: Add `@Pattern` (with a `message`) to `Order.ccExpiration`, requiring
  the `MM/YY` shape.
- TODO-03: Add `@Digits(integer = 3, fraction = 0)` (with a `message`) to
  `Order.ccCVV`.
- TODO-04: Implement `OrderController.processOrder()` — return `"orderForm"`
  when `errors.hasErrors()`, otherwise `"redirect:/orders/current"`.

Run the tests until they all pass.

## Running the Lab

From the project root:

```bash
mvn -pl spring-concepts/spring-mvc-bean-validation test
```

Or from the lab directory:

```bash
cd spring-concepts/spring-mvc-bean-validation
mvn test
```

## Bonus (Optional)

- TODO-05 (optional): Add `processOrderMisordered()`, a second handler for
  `POST /orders/misordered` with the same `@Valid Order order` and `Errors
  errors` parameters — but with an unrelated `@RequestParam(required = false)
  String note` parameter placed between them. Post an invalid order to it and
  compare the response with what `processOrder()` returns for the same data.
