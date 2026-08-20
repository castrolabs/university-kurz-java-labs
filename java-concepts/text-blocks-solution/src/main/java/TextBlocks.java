public class TextBlocks {

    public static String sqlQuery() {
        return """
                SELECT id, email
                  FROM customers
                 WHERE active = true
                """;
    }

    public static String htmlListNoTrailingNewline() {
        return """
                <ul>
                  <li>one</li>
                  <li>two</li>
                </ul>""";
    }

    public static String rightPaddedColors() {
        return """
                red  \s
                green\s
                blue
                """;
    }

    public static String wrapOneLine() {
        return """
                The quick brown fox \
                jumps over \
                the lazy dog.""";
    }

    public static String quotesInsideBlock() {
        return """
                Totals row ends with a literal \""" marker.""";
    }

    public static String normalizeFromExternalSource(String rawFileContent) {
        return rawFileContent.stripIndent();
    }

    public static String translateEscapesFromExternalSource(String rawFileContent) {
        return rawFileContent.translateEscapes();
    }
}
