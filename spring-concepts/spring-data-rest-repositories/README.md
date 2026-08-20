# Spring Data REST Repositories

## Goal

Expose a JPA repository as a full hypermedia REST API with zero
`@RestController` code, and prove -- by asserting on the actual HAL JSON
shape, not just HTTP status codes -- what Spring Data REST generates for
you and where its defaults need a nudge.

## Prerequisites

- Basic Spring Data JPA (`@Entity`, `@Id`, repository interfaces)
- Basic HAL/hypermedia concepts (`_links`, `_embedded`)

## Task

`Photo` isn't a JPA entity yet, and `PhotoRepository` is a bare
`CrudRepository` with no search capability. Once mapped, adding
`spring-boot-starter-data-rest` to the classpath (already done in this
lab's `pom.xml`) is enough to expose `Photo` as a full CRUD hypermedia API
-- but Spring Data REST's automatic pluralizer turns `Photo` into the
awkward `/photoes`, not the `/photos` you'd expect. Fixing that, and adding
a derived query method exposed as a search resource, are what the TODOs
below ask for.

The tests assert on the real HAL JSON returned by the running application
(`_links.self.href`, `_embedded.photos[...]`) using `MockMvc`, not just
status codes -- so getting the pluralization and the search resource wrong
shows up as a concrete, inspectable JSON mismatch.

## Instructions

Complete the following TODOs:

- TODO-00: Turn `Photo` into a JPA entity -- `@Entity`, `@Id` +
  `@GeneratedValue`.
- TODO-01: Fix the collection path with `@RepositoryRestResource(path =
  "photos", collectionResourceRel = "photos", itemResourceRel = "photos")`
  on `PhotoRepository`, so it's served at `/photos` instead of the default
  `/photoes`.
- TODO-02: Add `List<Photo> findByPhotographerIgnoreCase(@Param("photographer")
  String photographer)` to `PhotoRepository`, exposed automatically as a
  search resource at `/photos/search/findByPhotographerIgnoreCase`.

Run the tests until they all pass.

## Running the Lab

From the project root:

```bash
mvn -pl spring-concepts/spring-data-rest-repositories test
```

Or from the lab directory:

```bash
cd spring-concepts/spring-data-rest-repositories
mvn test
```

## Bonus (Optional)

- TODO-03 (optional): Restrict deletion by overriding `deleteById` on
  `PhotoRepository` with `@RestResource(exported = false)`, so `DELETE
  /photos/{id}` responds `405 Method Not Allowed` instead of succeeding.
  Exposure in Spring Data REST is opt-out, not opt-in -- every inherited
  repository method is reachable over HTTP unless explicitly turned off.
