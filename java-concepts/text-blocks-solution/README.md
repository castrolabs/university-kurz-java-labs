# Text Blocks - Solution

## Overview

This is the official solution for the Text Blocks lab. Each method demonstrates one
distinct mechanic of the text block algorithm: incidental-whitespace stripping keyed
off the closing delimiter's column, always-on trailing-whitespace stripping, the two
escapes unique to text blocks, and the runtime equivalent of the compile-time
algorithm.

## Key Concepts

### The closing delimiter's column is part of the stripping input

```java
public static String sqlQuery() {
    return """
            SELECT id, email
              FROM customers
             WHERE active = true
            """;
}
```

The compiler takes the minimum leading-whitespace count across every non-blank
content line *and* the line holding the closing `"""`, then strips that many columns
from every line. Here all four lines (three content lines plus the closing delimiter)
share the same base indentation, so that base is what gets stripped — leaving the
*relative* indentation (`  FROM`, ` WHERE`) intact.

### Where the closing delimiter sits controls the trailing newline

```java
public static String htmlListNoTrailingNewline() {
    return """
            <ul>
              <li>one</li>
              <li>two</li>
            </ul>""";
}
```

Putting `"""` directly after the last content character — instead of on its own
line — means there is no line terminator after that content, so the value simply
ends where the text does.

### Trailing whitespace is stripped independently of indentation

```java
public static String rightPaddedColors() {
    return """
            red  \s
            green\s
            blue
            """;
}
```

Every line's trailing whitespace is removed before anything else happens. `\s`
(a single space) is applied *after* that pass, so it is the only way to make a
space survive at the end of a line — two typed spaces plus `\s` on the `red` line
yields three real trailing spaces.

### Line continuation joins physical lines into one logical line

```java
public static String wrapOneLine() {
    return """
            The quick brown fox \
            jumps over \
            the lazy dog.""";
}
```

A backslash at the very end of a source line suppresses that line's terminator
instead of inserting one — useful for wrapping one long sentence across several
lines of source without embedding `\n` in the value. This escape does not exist
in ordinary `"..."` string literals.

### Only a run of three-or-more quotes needs escaping

```java
public static String quotesInsideBlock() {
    return """
            Totals row ends with a literal \""" marker.""";
}
```

A lone `"` or two in a row are never ambiguous with the block's delimiter. Escaping
just the first of three consecutive quotes (`\"""`) is enough to disambiguate the
rest.

### The same algorithm, exposed at runtime

```java
public static String normalizeFromExternalSource(String rawFileContent) {
    return rawFileContent.stripIndent();
}

public static String translateEscapesFromExternalSource(String rawFileContent) {
    return rawFileContent.translateEscapes();
}
```

`String.stripIndent()` and `String.translateEscapes()` expose the exact same
algorithms the compiler runs on a text block literal, for text that only exists at
runtime (read from a file, a network response, etc.) and therefore can never be a
compile-time literal.

## Summary

- The closing delimiter's column is a genuine input to the indentation-stripping
  algorithm, not just a terminator — moving it changes the value with no compiler
  warning.
- Trailing whitespace is always stripped from every line; `\s` is the only way to pin
  a space at the end of a line.
- The line-continuation `\` and `\s` escapes exist only inside text blocks.
- `String.stripIndent()` and `String.translateEscapes()` let you apply the same
  mechanics to runtime strings that were never text block literals.
