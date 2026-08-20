import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
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

    public static BigDecimal parseExact(String rawAmount) {
        return new BigDecimal(rawAmount);
    }

    public static BigDecimal shareOf(BigDecimal total, int numberOfParties, RoundingMode roundingMode) {
        return total.divide(BigDecimal.valueOf(numberOfParties), 2, roundingMode);
    }

    public static List<BigDecimal> splitEvenly(BigDecimal total, int numberOfParties) {
        if (numberOfParties <= 0) {
            throw new IllegalArgumentException("numberOfParties must be positive, got " + numberOfParties);
        }

        BigDecimal baseShare = shareOf(total, numberOfParties, RoundingMode.DOWN);
        BigDecimal distributed = baseShare.multiply(BigDecimal.valueOf(numberOfParties));
        BigDecimal remainder = total.subtract(distributed);

        BigDecimal cent = new BigDecimal("0.01");
        int leftoverCents = remainder.divide(cent, 0, RoundingMode.HALF_UP).intValueExact();

        List<BigDecimal> shares = new ArrayList<>(numberOfParties);
        for (int i = 0; i < numberOfParties; i++) {
            shares.add(i < leftoverCents ? baseShare.add(cent) : baseShare);
        }
        return shares;
    }

    public static BigDecimal convertCurrency(BigDecimal amount, BigDecimal exchangeRate,
                                              int targetScale, RoundingMode roundingMode) {
        return amount.multiply(exchangeRate).setScale(targetScale, roundingMode);
    }

    public static BigDecimal sumExact(List<BigDecimal> amounts) {
        BigDecimal total = BigDecimal.ZERO;
        for (BigDecimal amount : amounts) {
            total = total.add(amount);
        }
        return total;
    }
}
