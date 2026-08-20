# JSON Processing in Java - Solution

## Overview

This is the official solution for the `ManifestProcessor` lab. It uses Jackson 3 (`tools.jackson.*`, via the `JsonMapper` façade) and deliberately mixes two of Jackson's three processing models: data binding for the fields worth modeling as a `record`, and the tree model for fields that aren't.

## Key Concepts

### A plain JsonMapper already tolerates unknown fields — verify your defaults, don't guess them

```java
public static PackageManifest readManifest(String json, JsonMapper mapper) {
    return mapper.readValue(json, PackageManifest.class);
}
```

Records need no annotations for this: Jackson reads the component names straight off the class file. With the Jackson 3 version this project resolves (3.1.x, via the Spring Boot 4 BOM), `JsonMapper.builder().build()` already tolerates a JSON field that isn't a `PackageManifest` component — `DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES` defaults to `false`. That's good news for the "schema evolution shouldn't break deserialization" use case, but it also means you can't assume strictness without checking: two projects on two different Jackson versions can disagree about this default, so anywhere it actually matters, set it explicitly rather than relying on whatever the library happens to ship with.

### Opting in to strictness for the cases where an unknown field is the bug

```java
public static JsonMapper createStrictMapper() {
    return JsonMapper.builder()
            .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build();
}
```

Some documents are the opposite case: an internal API where an unexpected field usually means a typo or a stale contract, and you want that caught immediately rather than silently dropped. `createStrictMapper()` builds a second, independently configured `JsonMapper` that enables the same feature the default mapper leaves off, and `readManifest(json, createStrictMapper())` throws `UnrecognizedPropertyException` on exactly the same document the default mapper accepts. Jackson 3's `JsonMapper` is immutable once built — every knob is set through the `Builder`, unlike Jackson 2's mutable `ObjectMapper.disable(...)` called after construction — so each mapper you build is a fixed, independent policy rather than shared mutable state two call sites could fight over.

### path() over get() for anything optional

```java
public static String readHomepage(String json, JsonMapper mapper) {
    JsonNode root = mapper.readTree(json);
    return root.path("homepage").asText("unknown");
}
```

`get("homepage")` would return Java `null` for a document without that field, and calling `.asText()` on that `null` throws a `NullPointerException` — the chain breaks the moment the field is absent. `path("homepage")` returns a `MissingNode` instead, which keeps the chain alive; `asText("unknown")` supplies the default in the same call rather than a separate null-check.

### JSON Pointer for a nested field

```java
public static String readPublisherCity(String json, JsonMapper mapper) {
    JsonNode city = mapper.readTree(json).at("/publisher/city");
    return city.isMissingNode() ? "unknown" : city.asText();
}
```

`at("/publisher/city")` reaches straight into the nested object in one call instead of `path("publisher").path("city")`, and — like `path()` — returns `MissingNode` rather than throwing when either segment of the path doesn't exist, whether because `publisher` itself is missing or because it exists without a `city`.

### TypeReference for generic collections (bonus)

```java
public static List<PackageManifest> readManifests(String arrayJson, JsonMapper mapper) {
    return mapper.readValue(arrayJson, new TypeReference<List<PackageManifest>>() {
    });
}
```

A bare `Class` literal can't describe `List<PackageManifest>` — type erasure throws the element type away at compile time, so `mapper.readValue(arrayJson, List.class)` compiles but hands back a `List` of raw `LinkedHashMap`s, and the first `.get(0)` cast to `PackageManifest` throws `ClassCastException`. `TypeReference` is an anonymous subclass whose generic supertype Jackson can inspect via reflection, which is how it recovers the element type erasure destroyed.

## Summary

- Data binding, the tree model, and streaming are different tools for different documents — bind when you have (or want) a type, walk the tree when you don't, stream when the document is too big to hold in memory.
- Whether unknown fields are tolerated or rejected is a policy you set explicitly with `DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES` — don't assume either direction without checking the version actually on the classpath.
- `path()`/`at()` return a `MissingNode` instead of `null` or an exception — a miss becomes a value you test (`isMissingNode()`) or default (`asText("fallback")`), not a `NullPointerException` you have to guard against by hand.
- `TypeReference` is how a generic collection type survives erasure when handed to Jackson.
