import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Membership, TaxCalculator, and Receipt are already fully implemented in
 * src/main/java. Each @Nested group below targets one specific anti-pattern
 * from the article - read its class-level comment before writing the
 * tests inside it.
 */
class AntiPatternsTest {

    // Anti-pattern: testing private methods / exposing private state.
    // Membership.level is private with no getter, on purpose. The
    // temptation is to add a getLevel() (or widen visibility) just so a
    // test can confirm upgrade() worked. Don't - assert what a real caller
    // actually observes instead: discountRate().
    @Nested
    @DisplayName("avoiding private-state testing")
    class WhenTestingUpgrade {

        // TODO-00: new Membership(); assert discountRate() equals
        // BigDecimal.ZERO. Call upgrade(). Assert discountRate() now
        // equals new BigDecimal("0.15"). Do not add or use any getter for
        // the private `level` field - assert only through discountRate().
        @Test
        @DisplayName("upgrade() changes the discount rate a caller observes")
        void discountRateReflectsUpgrade() {
            fail("TODO-00: not implemented yet");
        }
    }

    // Anti-pattern: leaking domain knowledge into the test's arrange step.
    // A test that computes its "expected" tax as
    // subtotal.multiply(new BigDecimal("0.08")) has the exact same formula
    // (and the exact same bug, if there is one) as TaxCalculator itself -
    // it could never catch a real bug in that formula. Hardcode values you
    // work out by hand instead.
    @Nested
    @DisplayName("avoiding leaked domain knowledge")
    class WhenComputingTax {

        // TODO-01: Add a @CsvSource above this method with rows of
        // "subtotal, expectedTax" - HAND-COMPUTED values, not derived by
        // calling subtotal.multiply(RATE) inside the test. Cover at least
        // subtotal 10.00, 25.50, 100.00, and 0.00.
        @ParameterizedTest
        @DisplayName("calculate() returns the independently-verified tax amount")
        void calculateReturnsExpectedTax(BigDecimal subtotal, BigDecimal expectedTax) {
            assertEquals(expectedTax, new TaxCalculator().calculate(subtotal));
        }
    }

    // Anti-pattern: calling the system clock directly inside business
    // logic. Membership.renew(Clock) already takes the clock as a
    // parameter instead of calling Clock.systemDefaultZone() internally -
    // that's what makes it deterministically testable at all.
    @Nested
    @DisplayName("avoiding time-dependent tests")
    class WhenRenewing {

        // TODO-02: Build a fixed clock with
        // Clock.fixed(Instant.parse("2020-01-01T00:00:00Z"),
        // ZoneOffset.UTC). Call membership.renew(fixedClock). Assert
        // membership.getRenewedAt() equals the same fixed Instant - never
        // compare against Instant.now() (that would be nondeterministic).
        @Test
        @DisplayName("renew() records the exact instant of the fixed clock")
        void renewRecordsTheFixedInstant() {
            fail("TODO-02: not implemented yet");
        }
    }

    // TODO-03 (optional): Build a Receipt with subtotal
    // new BigDecimal("50.00") and a real `new TaxCalculator()` - not a
    // mock. TaxCalculator has no I/O, so mocking it would only add
    // indirection without buying anything. Assert summarize() equals a
    // hardcoded expected string you compute independently:
    // "Subtotal: 50.00, Tax: 4.00, Total: 54.00".
}
