# Class File Structure

## Goal

Understand what `javac` actually produces: a `.class` file with a rigid binary
layout — magic number, version, constant pool, access flags, fields, methods —
that the JVM trusts without ever seeing your source code. You'll parse the raw
bytes of a compiled class file yourself, and use reflection to confirm which
access flags the compiler wrote for an instance field versus a static field.

## Prerequisites

- Basic Java syntax
- `java.io.InputStream` / `DataInputStream`
- Reflection basics: `Class`, `Field`, `Modifier`

## Task

`Counter` is a small class with one instance field and one static field:

```java
public class Counter {
    int value;
    static int valueInstance;

    Counter(int newValue) {
        value = newValue;
        valueInstance = newValue;
    }
}
```

`value` lives once per object; `valueInstance` lives once per class, shared by
every instance. That difference isn't a runtime trick — it's encoded directly
in the class file as an access flag (`ACC_STATIC`) on the field, and reflected
in how the JVM allocates storage for it.

You'll work with two small tools that make the class file's structure
concrete instead of theoretical:

- `ClassFileReader` reads the raw bytes of `Counter.class` — the same bytes
  `javap` parses for you — and pulls out the magic number and version fields
  by hand.
- `FieldFlags` uses reflection to check whether a field's modifiers include
  `ACC_PUBLIC` or `ACC_STATIC`, the exact bits stored in the field's
  `access_flags` in the class file.

## Instructions

Complete the following TODOs:

- TODO-00 (`Counter.java`): Add the modifier that gives `value` the
  `ACC_PUBLIC` flag its comment promises.
- TODO-01 (`ClassFileReader.java`): Implement `readMagicNumber()` — read the
  first 4 bytes of the class file.
- TODO-02 (`ClassFileReader.java`): Implement `readMajorVersion()` — skip the
  magic number and minor version, then read the major version.
- TODO-03 (`FieldFlags.java`): Implement `isPublic()` using reflection.
- TODO-04 (`FieldFlags.java`): Implement `isStatic()` using reflection.

Run the tests until they all pass.

## Look at the real bytes

Before or after finishing the TODOs, compile the lab and inspect the actual
class file with `javap`:

```bash
mvn -pl jvm-concepts/class-file-structure compile
javap -v -p jvm-concepts/class-file-structure/target/classes/Counter.class
```

Find in the output:

- The magic number and version at the top.
- `value` and `valueInstance` in the `Fields:` section — check their `flags:`
  line against what `FieldFlagsTest` asserts.
- The constant pool table — notice it starts at `#1`, not `#0`.

## Running the Lab

From the project root:

```bash
mvn -pl jvm-concepts/class-file-structure test
```

Or from the lab directory:

```bash
cd jvm-concepts/class-file-structure
mvn test
```

## Bonus (Optional)

- TODO-05 (optional): Implement `readConstantPoolCount()` in
  `ClassFileReader` — skip magic number, minor version, and major version (8
  bytes total), then read the constant pool count. Compare it against the
  `#1`-based indexing you saw in the `javap` output.
