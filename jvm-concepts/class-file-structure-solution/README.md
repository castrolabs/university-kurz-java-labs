# Class File Structure - Solution

## Overview

This is the official solution for the Class File Structure lab. It parses a
compiled `.class` file's header by hand and uses reflection to confirm the
access flags the compiler wrote for an instance field versus a static field.

## Key Concepts

### The magic number is just the first 4 bytes

```java
public static int readMagicNumber(InputStream classFile) throws IOException {
    var in = new DataInputStream(classFile);
    return in.readInt();
}
```

`DataInputStream.readInt()` reads 4 bytes big-endian. `0xCAFEBABE` is larger
than `Integer.MAX_VALUE`, so as a signed `int` it prints negative — but the
literal `0xCAFEBABE` in source code represents the exact same bit pattern, so
comparing `readInt() == 0xCAFEBABE` is correct without any masking.

### The header is a fixed sequence, so later fields require skipping earlier ones

```java
public static int readMajorVersion(InputStream classFile) throws IOException {
    var in = new DataInputStream(classFile);
    in.readInt();                  // magic_number   (4 bytes)
    in.readUnsignedShort();        // minor_version   (2 bytes)
    return in.readUnsignedShort(); // major_version   (2 bytes)
}
```

There's no field named `major_version` to seek to — you get it by reading
(and discarding) everything that comes before it, in order. `minor_version`
and `major_version` are unsigned 16-bit values, so `readUnsignedShort()` is
used instead of `readShort()`.

### Reflection surfaces the exact bits the class file stores

```java
public static boolean isPublic(Class<?> type, String fieldName) throws NoSuchFieldException {
    Field field = type.getDeclaredField(fieldName);
    return Modifier.isPublic(field.getModifiers());
}
```

`Field.getModifiers()` isn't a JVM-independent abstraction — it returns the
same `access_flags` bitmask stored in the field's `field_info` structure in
the class file. `Modifier.isPublic()` / `Modifier.isStatic()` just test the
`ACC_PUBLIC` / `ACC_STATIC` bits.

### Instance vs. static storage, made concrete

```java
var a = new Counter(10);
var b = new Counter(12);
// a.value == 10, b.value == 12, Counter.valueInstance == 12
```

`value` has no `ACC_STATIC` flag, so each `Counter` object gets its own copy
— `a` and `b` never interfere. `valueInstance` has `ACC_STATIC` set, so there
is exactly one storage location on the `Counter` class itself; every
constructor call overwrites the same slot, which is why it ends up holding
`12`, the last value written, no matter how many instances exist.

## Summary

- `.class` files have a fixed byte layout — magic number, minor/major
  version, then the constant pool — and reading any field means reading
  (and skipping) everything before it.
- `0xCAFEBABE` is a normal `int` literal in Java; no unsigned trickery needed
  to compare it.
- `Field.getModifiers()` and `Modifier` expose the real `access_flags` bits
  from the class file — `javap -v -p` is reading the same information.
- `ACC_STATIC` is the entire reason a static field is shared across
  instances: it's not a JVM "trick", it's one bit in the field's flags that
  tells the JVM to allocate the storage once, on the class.
