# Text Blocks

## Goal

Understand that a text block's value is not simply "what you typed between the triple
quotes." Two mechanics decide the actual `String` you get: an incidental-whitespace
stripping algorithm where the closing delimiter's own column is part of the input, and
an always-on trailing-whitespace pass that runs independently of it. You'll also use
the two escapes that exist only inside text blocks (`\s` and line-continuation `\`),
and apply the same stripping algorithm at runtime via `String.stripIndent()`.

## Prerequisites

- Ordinary `String` literals and escapes (`\n`, `\t`, `\"`)
- Basic familiarity with `String` instance methods

## Task

`TextBlocks` has six required methods, each returning a `String` built from a text
block. Every method's Javadoc-style comment gives you the exact target value —
reproducing it forces you to reason about indentation stripping, trailing-whitespace
stripping, or one of the two text-block-only escapes, rather than letting you paste a
snippet and hope it matches.

## Instructions

Complete the following TODOs in `TextBlocks`:

- TODO-00: `sqlQuery()` — reproduce a multi-line value with relative indentation preserved and a trailing newline.
- TODO-01: `htmlListNoTrailingNewline()` — reproduce a multi-line value with NO trailing newline.
- TODO-02: `rightPaddedColors()` — preserve trailing spaces that would otherwise be stripped.
- TODO-03: `wrapOneLine()` — join three physical source lines into one logical line with no embedded `\n`.
- TODO-04: `quotesInsideBlock()` — produce a value containing three consecutive `"` characters.
- TODO-05: `normalizeFromExternalSource(String)` — apply the incidental-whitespace algorithm at runtime, to a `String` that did not come from a text block literal.

Run the tests until they all pass.

## Running the Lab

From the project root:

```bash
mvn -pl java-concepts/text-blocks test
```

Or from the lab directory:

```bash
cd java-concepts/text-blocks
mvn test
```

## Bonus (Optional)

- TODO-06 (optional): `translateEscapesFromExternalSource(String)` — convert literal escape sequences in a runtime `String` (e.g. a backslash followed by `n`) into their real characters, the way text block content is escape-processed after whitespace stripping.
