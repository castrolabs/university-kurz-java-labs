import java.math.BigDecimal;

/**
 * Formats a subtotal and its tax into a printable summary. TaxCalculator
 * is a plain, deterministic collaborator with no I/O of its own - real
 * instances are cheap enough that tests can use one directly instead of
 * mocking it.
 */
public class Receipt {

    private final BigDecimal subtotal;
    private final TaxCalculator taxCalculator;

    public Receipt(BigDecimal subtotal, TaxCalculator taxCalculator) {
        this.subtotal = subtotal;
        this.taxCalculator = taxCalculator;
    }

    public String summarize() {
        BigDecimal tax = taxCalculator.calculate(subtotal);
        BigDecimal total = subtotal.add(tax);
        return "Subtotal: " + subtotal + ", Tax: " + tax + ", Total: " + total;
    }
}
