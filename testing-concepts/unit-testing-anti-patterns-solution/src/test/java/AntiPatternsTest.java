import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AntiPatternsTest {

    @Nested
    @DisplayName("avoiding private-state testing")
    class WhenTestingUpgrade {

        @Test
        @DisplayName("upgrade() changes the discount rate a caller observes")
        void discountRateReflectsUpgrade() {
            Membership membership = new Membership();
            assertEquals(BigDecimal.ZERO, membership.discountRate());

            membership.upgrade();

            assertEquals(new BigDecimal("0.15"), membership.discountRate());
        }
    }

    @Nested
    @DisplayName("avoiding leaked domain knowledge")
    class WhenComputingTax {

        @ParameterizedTest
        @DisplayName("calculate() returns the independently-verified tax amount")
        @CsvSource({
                "0.00, 0.00",
                "10.00, 0.80",
                "25.50, 2.04",
                "100.00, 8.00"
        })
        void calculateReturnsExpectedTax(BigDecimal subtotal, BigDecimal expectedTax) {
            assertEquals(expectedTax, new TaxCalculator().calculate(subtotal));
        }
    }

    @Nested
    @DisplayName("avoiding time-dependent tests")
    class WhenRenewing {

        @Test
        @DisplayName("renew() records the exact instant of the fixed clock")
        void renewRecordsTheFixedInstant() {
            Instant fixedInstant = Instant.parse("2020-01-01T00:00:00Z");
            Clock fixedClock = Clock.fixed(fixedInstant, ZoneOffset.UTC);
            Membership membership = new Membership();

            membership.renew(fixedClock);

            assertEquals(fixedInstant, membership.getRenewedAt());
        }
    }

    @Test
    @DisplayName("bonus: summarize() formats the subtotal, tax, and total using a real TaxCalculator")
    void summarizeFormatsSubtotalTaxAndTotal() {
        Receipt receipt = new Receipt(new BigDecimal("50.00"), new TaxCalculator());

        assertEquals("Subtotal: 50.00, Tax: 4.00, Total: 54.00", receipt.summarize());
    }
}
