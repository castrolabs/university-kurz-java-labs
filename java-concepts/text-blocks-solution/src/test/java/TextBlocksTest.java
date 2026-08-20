import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TextBlocks")
class TextBlocksTest {

    @Test
    @DisplayName("sqlQuery should preserve relative indentation and end with a trailing newline")
    void sqlQueryShouldPreserveRelativeIndentationAndTrailingNewline() {
        String expected = "SELECT id, email\n"
                + "  FROM customers\n"
                + " WHERE active = true\n";

        assertEquals(expected, TextBlocks.sqlQuery());
    }

    @Test
    @DisplayName("htmlListNoTrailingNewline should have no trailing newline")
    void htmlListShouldHaveNoTrailingNewline() {
        String expected = "<ul>\n"
                + "  <li>one</li>\n"
                + "  <li>two</li>\n"
                + "</ul>";

        String actual = TextBlocks.htmlListNoTrailingNewline();

        assertEquals(expected, actual);
        assertFalse(actual.endsWith("\n"), "value must not end with a newline");
    }

    @Test
    @DisplayName("rightPaddedColors should preserve trailing spaces via \\s")
    void rightPaddedColorsShouldPreserveTrailingSpaces() {
        String expected = "red   \n" + "green \n" + "blue\n";

        assertEquals(expected, TextBlocks.rightPaddedColors());
    }

    @Test
    @DisplayName("wrapOneLine should join three source lines into one logical line")
    void wrapOneLineShouldJoinSourceLinesWithoutNewlines() {
        String actual = TextBlocks.wrapOneLine();

        assertEquals("The quick brown fox jumps over the lazy dog.", actual);
        assertFalse(actual.contains("\n"), "value must not contain an embedded newline");
    }

    @Test
    @DisplayName("quotesInsideBlock should contain three consecutive double quotes")
    void quotesInsideBlockShouldContainThreeConsecutiveQuotes() {
        String actual = TextBlocks.quotesInsideBlock();

        assertEquals("Totals row ends with a literal \"\"\" marker.", actual);
        assertTrue(actual.contains("\"\"\""), "value must contain three consecutive quote characters");
    }

    @Test
    @DisplayName("normalizeFromExternalSource should strip the minimum common indentation at runtime")
    void normalizeFromExternalSourceShouldStripMinimumCommonIndentation() {
        String raw = "  line one\n" + "    line two\n" + "  line three";

        String expected = "line one\n" + "  line two\n" + "line three";

        assertEquals(expected, TextBlocks.normalizeFromExternalSource(raw));
    }

    @Test
    @DisplayName("normalizeFromExternalSource should return an empty string for an empty input")
    void normalizeFromExternalSourceShouldHandleEmptyInput() {
        assertEquals("", TextBlocks.normalizeFromExternalSource(""));
    }

    @Test
    @DisplayName("translateEscapesFromExternalSource should convert literal escape sequences (bonus)")
    void translateEscapesFromExternalSourceShouldConvertLiteralEscapes() {
        String raw = "first\\nsecond\\tthird";

        assertEquals("first\nsecond\tthird", TextBlocks.translateEscapesFromExternalSource(raw));
    }
}
