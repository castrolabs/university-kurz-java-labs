import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MoneySplitter")
class MoneySplitterTest {

    @Test
    @DisplayName("should parse a decimal string into the exact same digits")
    void shouldParseDecimalStringExactly() {
        assertEquals("19.99", MoneySplitter.parseExact("19.99").toPlainString());
    }

    @Test
    @DisplayName("should preserve scale from the original text (2.50, not 2.5)")
    void shouldPreserveScaleFromText() {
        assertEquals(2, MoneySplitter.parseExact("2.50").scale());
        assertEquals("2.50", MoneySplitter.parseExact("2.50").toPlainString());
    }

    @Test
    @DisplayName("should never introduce binary floating-point error: 0.1 + 0.2 must be exactly 0.3")
    void shouldAvoidBinaryFloatingPointImprecision() {
        BigDecimal sum = MoneySplitter.parseExact("0.1").add(MoneySplitter.parseExact("0.2"));

        assertEquals(0, sum.compareTo(new BigDecimal("0.3")),
                "0.1 + 0.2 must compare equal to 0.3 — if this fails, parseExact() routed through a double somewhere");
        assertEquals("0.3", sum.toPlainString(),
                "sum should print as a clean \"0.3\", not a long binary-approximation tail");
    }

    @Test
    @DisplayName("should throw for a null amount, just like the BigDecimal(String) constructor does")
    void shouldThrowForNullAmount() {
        assertThrows(NullPointerException.class, () -> MoneySplitter.parseExact(null));
    }

    @Test
    @DisplayName("should compute one share honoring the given RoundingMode (not a hardcoded one)")
    void shouldComputeShareUsingGivenRoundingMode() {
        BigDecimal total = new BigDecimal("5");

        assertEquals(new BigDecimal("1.67"), MoneySplitter.shareOf(total, 3, RoundingMode.HALF_UP));
        assertEquals(new BigDecimal("1.66"), MoneySplitter.shareOf(total, 3, RoundingMode.DOWN));
    }

    @Test
    @DisplayName("should split a total that divides evenly with no leftover cents")
    void shouldSplitEvenlyWithNoRemainder() {
        List<BigDecimal> shares = MoneySplitter.splitEvenly(new BigDecimal("9.00"), 3);

        assertEquals(
                List.of(new BigDecimal("3.00"), new BigDecimal("3.00"), new BigDecimal("3.00")),
                shares);
    }

    @Test
    @DisplayName("should distribute leftover cents to the first parties so the shares sum exactly to the total")
    void shouldDistributeRemainderCentsToFirstParties() {
        List<BigDecimal> shares = MoneySplitter.splitEvenly(new BigDecimal("10.00"), 3);

        assertEquals(
                List.of(new BigDecimal("3.34"), new BigDecimal("3.33"), new BigDecimal("3.33")),
                shares);
    }

    @Test
    @DisplayName("should always produce shares that sum back to exactly the original total")
    void shouldProduceSharesSummingToTotal() {
        BigDecimal total = new BigDecimal("100.00");
        List<BigDecimal> shares = MoneySplitter.splitEvenly(total, 7);

        BigDecimal sum = shares.stream().reduce(BigDecimal.ZERO, BigDecimal::add);

        assertEquals(0, sum.compareTo(total),
                "shares should sum to exactly " + total + " but summed to " + sum);
        assertEquals(7, shares.size());
    }

    @Test
    @DisplayName("should throw for a non-positive number of parties")
    void shouldThrowForNonPositiveNumberOfParties() {
        assertThrows(IllegalArgumentException.class, () -> MoneySplitter.splitEvenly(new BigDecimal("10.00"), 0));
        assertThrows(IllegalArgumentException.class, () -> MoneySplitter.splitEvenly(new BigDecimal("10.00"), -1));
    }

    @Test
    @DisplayName("should convert currency using an explicit scale and RoundingMode")
    void shouldConvertCurrencyUsingExplicitScaleAndRoundingMode() {
        BigDecimal amount = new BigDecimal("1");
        BigDecimal rate = new BigDecimal("0.125");

        assertEquals(new BigDecimal("0.13"),
                MoneySplitter.convertCurrency(amount, rate, 2, RoundingMode.HALF_UP));
        assertEquals(new BigDecimal("0.12"),
                MoneySplitter.convertCurrency(amount, rate, 2, RoundingMode.HALF_EVEN));
    }

    @Test
    @DisplayName("should sum a list of amounts exactly (bonus)")
    void shouldSumAmountsExactly() {
        List<BigDecimal> amounts = List.of(
                new BigDecimal("10.00"), new BigDecimal("5.50"), new BigDecimal("14.50"));

        assertEquals(0, MoneySplitter.sumExact(amounts).compareTo(new BigDecimal("30.00")));
    }

    @Test
    @DisplayName("should return zero for an empty list of amounts (bonus)")
    void shouldReturnZeroForEmptyAmountList() {
        assertEquals(0, MoneySplitter.sumExact(List.of()).compareTo(BigDecimal.ZERO));
    }
}
