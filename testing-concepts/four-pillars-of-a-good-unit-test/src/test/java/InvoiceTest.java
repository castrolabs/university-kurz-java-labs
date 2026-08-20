import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Invoice is already fully implemented in src/main/java. Each TODO below
 * targets one of the article's four pillars - protection against
 * regressions, resistance to refactoring, fast feedback, and
 * maintainability - by asking for a specific kind of test, not just "a
 * test that passes."
 */
class InvoiceTest {

    // TODO-00 (protection against regressions): build an Invoice with a
    // single LineItem("Widget", 3, new BigDecimal("40.00")) - a subtotal of
    // 120.00, above the discount threshold. Assert total() equals a
    // HARDCODED BigDecimal("108.00") (120.00 minus a 10% discount) that you
    // compute by hand, not by re-deriving it from Invoice's own discount
    // formula. A hardcoded expectation is what gives this test any chance
    // of catching a real bug in the discount math.
    @Test
    @DisplayName("total() applies the bulk discount above the threshold")
    void totalAppliesBulkDiscountAboveThreshold() {
        fail("TODO-00: not implemented yet");
    }

    // TODO-01 (protection against regressions, boundary case): a single
    // LineItem priced at exactly 99.99 (just under the 100.00 threshold).
    // Assert total() equals BigDecimal("99.99") - no discount applied.
    @Test
    @DisplayName("total() applies no discount just below the threshold")
    void totalHasNoDiscountJustBelowThreshold() {
        fail("TODO-01: not implemented yet");
    }

    // TODO-02 (resistance to refactoring): build an Invoice with THREE
    // LineItems whose lineTotal()s sum to EXACTLY 100.00 (e.g. 50.00 +
    // 30.00 + 20.00, quantity 1 each). Assert total() equals
    // BigDecimal("90.00"). Assert this ONLY by calling total() - never
    // reference computeSubtotal() (it's private, and it should stay that
    // way) and never add a getter to expose the running subtotal. That's
    // what lets this test keep passing if computeSubtotal() is later
    // renamed, inlined, or rewritten to sum differently - a pure refactor
    // that doesn't change what total() returns.
    @Test
    @DisplayName("total() sums multiple line items and applies the discount at the exact boundary")
    void totalSumsMultipleLineItemsAtExactBoundary() {
        fail("TODO-02: not implemented yet");
    }

    // TODO-03 (maintainability / fast feedback): an Invoice built from
    // List.of() (no line items). Assert total() equals BigDecimal("0.00").
    // No fixture setup beyond the empty list - this is the kind of test
    // that stays cheap to read and cheap to keep working even as Invoice
    // grows more line items and rules elsewhere.
    @Test
    @DisplayName("total() of an empty invoice is zero")
    void totalOfEmptyInvoiceIsZero() {
        fail("TODO-03: not implemented yet");
    }

    // TODO-04 (optional - illustrates the "trivial test" trade-off): build
    // an Invoice with three arbitrary LineItems and assert itemCount()
    // equals 3. This test is fast and about as resistant to refactoring as
    // a test can be - but itemCount() is a one-line delegation to
    // items.size(), so there's almost nowhere for a bug to hide. It scores
    // well on two pillars and close to zero on protection against
    // regressions - keep that contrast in mind against TODO-00.
}
