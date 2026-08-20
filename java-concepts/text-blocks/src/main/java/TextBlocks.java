public class TextBlocks {

    public static String sqlQuery() {
        // TODO-00: Return a text block whose value is exactly:
        //   "SELECT id, email\n" +
        //   "  FROM customers\n" +
        //   " WHERE active = true\n"
        // (three lines, each ending in \n — including the last one).
        // Hint: the compiler looks at every non-blank content line AND the line that
        // holds the closing delimiter, takes the LEAST indented one, and strips that
        // many leading spaces from every line.
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public static String htmlListNoTrailingNewline() {
        // TODO-01: Return a text block whose value is exactly:
        //   "<ul>\n" +
        //   "  <li>one</li>\n" +
        //   "  <li>two</li>\n" +
        //   "</ul>"
        // Note there is NO trailing newline this time — the value must not end in "\n".
        // Hint: whether the closing delimiter sits on its own line, or right after the
        // last content character, decides whether the value ends with \n.
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public static String rightPaddedColors() {
        // TODO-02: Return a text block whose value is exactly:
        //   "red   \n" +   (3 trailing spaces after "red")
        //   "green \n" +   (1 trailing space after "green")
        //   "blue\n"       (no trailing space)
        // Hint: trailing white space is ALWAYS stripped from the end of every text
        // block line, no matter how many spaces you type there. One escape survives
        // this stripping because it is applied afterward — use it to pin a space in
        // place.
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public static String wrapOneLine() {
        // TODO-03: Return a text block whose value is exactly the single line:
        //   "The quick brown fox jumps over the lazy dog."
        // with NO embedded \n at all, even though you must write it across three
        // physical source lines for readability.
        // Hint: a backslash at the very end of a source line suppresses that line's
        // terminator, joining it with the next physical line. This escape only exists
        // in text blocks.
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public static String quotesInsideBlock() {
        // TODO-04: Return a text block whose value is exactly:
        //   Totals row ends with a literal """ marker.
        // (that literal text contains three consecutive double quote characters)
        // Hint: a lone " or two in a row need no escaping inside a text block; only a
        // run of three or more consecutive quotes is ambiguous with the closing
        // delimiter, so only one of them needs an escape.
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public static String normalizeFromExternalSource(String rawFileContent) {
        // TODO-05: `rawFileContent` simulates text that was just read from a file at
        // runtime, so it cannot be written as a compile-time text block literal.
        // Apply the SAME incidental-whitespace-stripping algorithm text blocks use at
        // compile time, but do it at runtime against this arbitrary String.
        // Hint: java.lang.String exposes this exact algorithm as an instance method —
        // no manual line splitting required.
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public static String translateEscapesFromExternalSource(String rawFileContent) {
        // TODO-06 (optional): `rawFileContent` contains literal two-character escape
        // sequences (a backslash followed by a letter, e.g. \n or \t) that should be
        // converted to their real single-character meaning, exactly the way a text
        // block's content is escape-processed after whitespace stripping.
        // Hint: java.lang.String exposes this translation as an instance method too.
        throw new UnsupportedOperationException("Not implemented yet.");
    }
}
