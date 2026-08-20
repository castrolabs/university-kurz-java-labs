import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Small money-handling utilities built on {@link BigDecimal}.
 *
 * <p>Every amount here is meant to represent currency, so every method must
 * produce an <em>exact</em> result — no value may ever pass through a
 * {@code double} on its way in or out.
 */
public class MoneySplitter {

    private MoneySplitter() {
    }

    /**
     * Parses a decimal string (e.g. "19.99") into an exact {@link BigDecimal}.
     *
     * <p>The result must carry exactly the digits that were written — no
     * binary floating-point value may be involved at any point.
     */
    public static BigDecimal parseExact(String rawAmount) {
        // TODO-00: Build the BigDecimal directly from the text. Do not route
        // through Double.parseDouble(...) or new BigDecimal(double) anywhere
        // in this method — both would import binary floating-point error
        // into a value that was written as exact decimal text.

        throw new UnsupportedOperationException("Not implemented yet.");
    }

    /**
     * Returns one party's share of {@code total} split evenly among
     * {@code numberOfParties}, rounded to 2 decimal places using the given
     * {@code roundingMode}.
     *
     * <p>This is the "one share" building block — it does not attempt to
     * make several shares add back up to the total; {@link #splitEvenly}
     * does that.
     */
    public static BigDecimal shareOf(BigDecimal total, int numberOfParties, RoundingMode roundingMode) {
        // TODO-01: Divide total by numberOfParties, scaled to 2 decimal
        // places, using the roundingMode argument (not a hardcoded one).
        // Passing a scale AND a RoundingMode to divide() is required here —
        // the single-argument divide(BigDecimal) throws ArithmeticException
        // the moment the division doesn't terminate (e.g. 10 / 3).

        throw new UnsupportedOperationException("Not implemented yet.");
    }

    /**
     * Splits {@code total} evenly across {@code numberOfParties}, returning
     * one share per party. Unlike {@link #shareOf}, the returned shares are
     * guaranteed to sum back to exactly {@code total} — any leftover cents
     * from rounding are distributed one cent at a time to the first parties
     * in the list.
     *
     * @throws IllegalArgumentException if numberOfParties is not positive
     */
    public static List<BigDecimal> splitEvenly(BigDecimal total, int numberOfParties) {
        // TODO-02: Validate numberOfParties > 0, then compute numberOfParties
        // equal shares (RoundingMode.DOWN keeps every share at or below the
        // true value) and distribute the leftover cents — the difference
        // between total and the sum of the rounded-down shares, expressed in
        // whole cents — by adding one cent each to the first N shares.
        // Every value must stay a BigDecimal with scale 2; reassign the
        // result of every add()/subtract() call, since BigDecimal is
        // immutable and a discarded return value silently does nothing.

        throw new UnsupportedOperationException("Not implemented yet.");
    }

    /**
     * Converts {@code amount} using {@code exchangeRate}, rounding the
     * product to {@code targetScale} decimal places with {@code roundingMode}.
     */
    public static BigDecimal convertCurrency(BigDecimal amount, BigDecimal exchangeRate,
                                              int targetScale, RoundingMode roundingMode) {
        // TODO-03: Multiply amount by exchangeRate (multiply() always has an
        // exact answer, so it never needs a RoundingMode), then round the
        // product to targetScale decimal places using roundingMode.

        throw new UnsupportedOperationException("Not implemented yet.");
    }

    /**
     * Sums a list of amounts exactly, without discarding any intermediate
     * result. Returns {@link BigDecimal#ZERO} for an empty list.
     */
    public static BigDecimal sumExact(List<BigDecimal> amounts) {
        // TODO-04 (optional): Add every amount together, starting from
        // BigDecimal.ZERO. Remember add() returns a new BigDecimal instead
        // of mutating either operand.

        throw new UnsupportedOperationException("Not implemented yet.");
    }
}
