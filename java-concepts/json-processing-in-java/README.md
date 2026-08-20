# JSON Processing in Java

## Goal

Learn to deliberately choose between Jackson's three processing models — data binding a JSON document straight into a record, and navigating the tree model (`JsonNode`, `path()`, JSON Pointer) for a field you didn't bother to model as a class — and how to control unknown-field handling explicitly instead of relying on a guess about what the default is.

## Prerequisites

- Basic Java syntax
- Java records

## Task

Implement `ManifestProcessor`, which reads a package-manifest JSON document (name, version, description, contributors) into a `PackageManifest` record, reads two fields that are *not* part of that record (`homepage`, and a nested `publisher.city`) through the tree model, and builds a strict `JsonMapper` for the situations where an unmodeled field should be treated as a bug, not silently ignored.

## Instructions

Complete the following TODOs in `ManifestProcessor`:

- TODO-00: Data-bind a JSON document into a `PackageManifest`.
- TODO-01: Build a `JsonMapper` that rejects unknown JSON fields instead of ignoring them.
- TODO-02: Read the `homepage` field via the tree model, defaulting to "unknown" when it's absent.
- TODO-03: Read the nested `publisher.city` field via a JSON Pointer, defaulting to "unknown" when either part is missing.

Run the tests until they all pass. One test (`shouldToleratesUnknownFieldByDefault`) only depends on TODO-00 — it exists to show you that a plain `JsonMapper` already tolerates an unmodeled field with zero extra configuration, which is exactly why TODO-01 has to opt *in* to strictness rather than opt out of it.

## Running the Lab

From the project root:

```bash
mvn -pl java-concepts/json-processing-in-java test
```

Or from the lab directory:

```bash
cd java-concepts/json-processing-in-java
mvn test
```

## Bonus (Optional)

- TODO-04 (optional): Data-bind a JSON array into a `List<PackageManifest>` using `TypeReference`, avoiding the `ClassCastException` trap of binding straight to a raw `List.class`.
