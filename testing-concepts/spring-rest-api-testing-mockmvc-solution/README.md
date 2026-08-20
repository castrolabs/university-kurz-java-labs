# Spring REST API Testing with MockMvc — Solution

## Overview

`ItemControllerTest` drives the already-implemented `ItemController` through
`MockMvc`, with `ItemService` replaced by a `@MockitoBean` so the test isolates the
web layer from the real catalog implementation.

## Key Concepts

- **`@WebMvcTest(ItemController.class)` loads a slice, not the whole app.** It wires
  up MVC infrastructure (`DispatcherServlet`, argument resolvers, message converters)
  and the named controller, but does not component-scan for `@Service`/`@Repository`
  beans. `sliceNeverLoadsRealServiceImplementation` proves this directly:
  `applicationContext.getBean(ItemServiceImpl.class)` throws
  `NoSuchBeanDefinitionException` even though `ItemServiceImpl` sits right next to
  `ItemController` on the classpath.
- **`@MockitoBean` fills the gap a slice deliberately leaves.** Because the slice
  won't wire a real `ItemService`, `ItemController`'s constructor can't be satisfied
  without *something* implementing that interface — `@MockitoBean` registers a mock
  in the context in its place, which is what makes `@WebMvcTest` usable for a
  controller with collaborators at all.
- **`jsonPath` asserts on the response shape, not the raw string.** `getAllReturnsEveryItem`
  and `getByIdReturnsItemWhenPresent` check specific fields (`$[0].name`, `$.id`)
  rather than comparing the whole JSON body, so the test survives unrelated field
  additions and still catches a wrong or missing value.
- **The 404 path is a real behavior to test, not an afterthought.** `ItemController`
  maps a missing item to `ResponseEntity.notFound()` explicitly — `getByIdReturns404WhenAbsent`
  is what would catch a regression to "return `null` with `200 OK`", a bug `jsonPath`
  alone on the happy path would never surface.
- **`verify()` checks the mock was called correctly, not just that it returned
  something useful.** The bonus test confirms `findById` was called with the exact
  id from the URL, which the happy-path test alone doesn't establish — a controller
  that always looked up id `1` regardless of the path variable would still pass
  `getByIdReturnsItemWhenPresent`.

## Summary

A `@WebMvcTest` slice is fast precisely because it refuses to load anything outside
the web layer — the trade-off is that every collaborator the controller needs must
be supplied as a `@MockitoBean`, and the test's job then splits in two: prove the
controller talks to that collaborator correctly (`verify`), and prove it turns the
collaborator's results into the right HTTP response (`jsonPath`, status codes).
