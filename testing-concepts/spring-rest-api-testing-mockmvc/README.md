# Spring REST API Testing with MockMvc

## Goal

`ItemController` and `ItemServiceImpl` are already fully implemented — your job is to
test the controller at the web layer using `MockMvc`, with the real service replaced
by a Mockito mock via `@WebMvcTest`.

## Prerequisites

- Test Doubles: Stubs and Mocking (Mockito `when`/`verify`)
- Reading `ItemController`, `ItemService`, and `ItemServiceImpl` in `src/main/java`
  before writing any test

## Task

`ItemController` depends on `ItemService` through its constructor. `@WebMvcTest`
loads only the MVC infrastructure and the controller you name — it does **not**
component-scan the rest of the application, so the real `ItemServiceImpl`
(a plain `@Service`) is never wired into this test's context. You supply a
`@MockitoBean` in its place and script it per test.

## Instructions

Complete the following TODOs in `ItemControllerTest`:

- TODO-00: `GET /items` returns every item the (mocked) service reports — assert on
  the JSON array with `jsonPath`.
- TODO-01: `GET /items/{id}` returns `200 OK` with the item's JSON body when the
  service finds it.
- TODO-02: `GET /items/{id}` returns `404 Not Found` when the service returns
  `Optional.empty()`.
- TODO-03: prove the slice never loaded the real `ItemServiceImpl` bean — only the
  `@MockitoBean` substitute — by asserting `applicationContext.getBean(ItemServiceImpl.class)`
  throws `NoSuchBeanDefinitionException`.

Run the tests until they all pass.

## Running the Lab

From the project root:

```bash
mvn -pl testing-concepts/spring-rest-api-testing-mockmvc test
```

Or from the lab directory:

```bash
cd testing-concepts/spring-rest-api-testing-mockmvc
mvn test
```

## Bonus (Optional)

- TODO-04 (optional): use `Mockito.verify(itemService)` to assert `getById(...)`
  called `itemService.findById(...)` with the exact id from the path variable, not
  just that the response looked right.
