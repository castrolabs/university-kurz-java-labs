# Spring Data REST Repositories - Solution

## Overview

This is the official solution for the Spring Data REST Repositories lab. It
maps `Photo` as a JPA entity and exposes `PhotoRepository` as a full HAL
hypermedia REST API, fixing the default pluralization and adding a
search resource -- with zero hand-written controller code.

## Key Concepts

### Zero-controller REST from a repository

```java
@Entity
public class Photo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String caption;
    private String photographer;
}
```

With `spring-boot-starter-data-rest` on the classpath, that's already
enough for `GET /photos`, `POST /photos`, `PUT /photos/{id}`, and `DELETE
/photos/{id}` to exist and return HAL-shaped JSON -- no `@RestController`
anywhere.

### Fixing the pluralizer

```java
@RepositoryRestResource(path = "photos", collectionResourceRel = "photos",
        itemResourceRel = "photos")
public interface PhotoRepository extends CrudRepository<Photo, Long> {
```

Spring Data REST derives a resource's path and relation name by pluralizing
the entity's simple class name. Its pluralizer isn't infallible: `Photo`
becomes `/photoes`, technically discoverable via the API's home resource but
not a URL any client would guess. `@RepositoryRestResource` pins both the
path and the relation name explicitly, the same fix the article shows for
`Taco` -> `/tacoes`.

`collectionUsesFixedPluralPath()` proves the fix concretely: `/photos`
returns 200, `/photoes` returns 404.

### A derived query becomes a search resource, automatically

```java
List<Photo> findByPhotographerIgnoreCase(@Param("photographer") String photographer);
```

Spring Data parses this method name into a query the same way any Spring
Data repository would -- and Spring Data REST additionally exposes it as
`GET /photos/search/findByPhotographerIgnoreCase?photographer=...`,
returning the same HAL-shaped `_embedded.photos` collection as the base
resource. No `@Query`, no controller. `@Param("photographer")` is required
here: this module doesn't compile with `-parameters`, so Spring Data can't
otherwise recover the parameter's name at runtime to bind the query string
to it -- without it, the endpoint 500s with "Unable to detect parameter
names".

### Restricting an inherited method (`TODO-03`, optional)

```java
@Override
@RestResource(exported = false)
void deleteById(Long id);
```

Every method a Spring Data repository interface declares -- including ones
it merely inherits, like `deleteById` from `CrudRepository` -- is reachable
over HTTP by default. Exposure is opt-out, not opt-in: restricting `DELETE
/photos/{id}` means overriding the method here purely to attach
`@RestResource(exported = false)`, which turns that endpoint into a 405
without touching the JPA-level delete logic Spring Data still generates.

## Trade-offs

- The persistence model *is* the API surface here: every field on `Photo`
  and every un-restricted method on `PhotoRepository` is reachable exactly
  as declared. A field renamed for persistence reasons changes the wire
  format for every client, with no version boundary in between.
- Automatic pluralization is convenient until an entity's name pluralizes
  irregularly (as `Photo` does) -- and the mismatch is only obvious once
  discovered via the API's home resource or a broken client integration.
- `@RepositoryRestResource` and `@RestResource(exported = false)` are two
  more annotations to learn, but they're still far less code than the
  hand-rolled `@RestController` + link-builder approach they replace for
  the CRUD majority of an API.

## Summary

- `spring-boot-starter-data-rest` plus a Spring Data repository is enough
  for a full HAL hypermedia CRUD API -- no controller required.
- Resource paths and relation names default to a pluralized class name that
  isn't always correct; `@RepositoryRestResource(rel, path)` overrides both.
- Derived query methods (`findBy...`) are exposed automatically as search
  resources under `/search`, using the same method-name-to-query parsing as
  any other Spring Data repository.
- Every repository method is exported by default; `@RestResource(exported =
  false)` is how you opt a specific one back out.
