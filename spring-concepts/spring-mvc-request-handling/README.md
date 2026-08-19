# Spring MVC Request Handling

## Goal

Understand Spring MVC's classic request-handling flow: a `@GetMapping` handler builds a
`Model` for a view, a `@PostMapping` handler receives a plain Java object with its fields
already bound from the submitted form (no explicit binding code), and a `redirect:`-prefixed
view name issues a real HTTP redirect instead of rendering a template.

## Prerequisites

- Basic Spring MVC (`@Controller`, `@RequestMapping`, `@GetMapping`, `@PostMapping`)
- Familiarity with HTTP status codes, especially 3xx redirects
- Basic understanding of the `Model` abstraction

## Task

`SignupController` handles a two-step signup flow: `GET /signup` shows an empty form,
`POST /signup` binds the submitted fields onto a `SignupForm` object and redirects the browser
to `GET /signup/success`. You'll implement the model wiring and the redirect.

The test uses `@WebMvcTest` with `MockMvc` — a fast slice test for the web layer that doesn't
need a real template engine to verify view names, model attributes, and redirects.

## Instructions

Complete the following TODOs in `SignupController`:

- TODO-00: In `showSignupForm`, add a new `SignupForm` to the model under the attribute name
  `"signupForm"`, then return the view name `"signup-form"`.
- TODO-01: In `processSignup`, add the submitted `username` to `redirectAttributes` as a flash
  attribute named `"username"`.
- TODO-02: In `processSignup`, return the redirect view name that sends the browser to
  `/signup/success` — remember the `redirect:` prefix.

Run the tests until they all pass.

## Running the Lab

From the project root:

```bash
mvn -pl spring-concepts/spring-mvc-request-handling test
```

Or from the lab directory:

```bash
cd spring-concepts/spring-mvc-request-handling
mvn test
```

## Bonus (Optional)

- TODO-03 (optional): Add an explicit `@ModelAttribute("signupForm")` annotation to the `form`
  parameter of `processSignup`. Implicit binding already works without it, but Spring's docs
  recommend the explicit form for GraalVM native-image AOT hint generation.
