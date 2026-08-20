import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * A pure, deterministic calculation with no I/O - there is nothing here
 * worth mocking. Split out of {@link Receipt} on purpose, so it can be
 * tested with plain input/output values.
 */
public class TaxCalculator {

    private static final BigDecimal RATE = new BigDecimal("0.08");

    public BigDecimal calculate(BigDecimal subtotal) {
        return subtotal.multiply(RATE).setScale(2, RoundingMode.HALF_UP);
    }
}
