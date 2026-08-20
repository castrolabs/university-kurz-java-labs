import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;

/**
 * A membership that can be upgraded to PREMIUM (changing its discount
 * rate) and renewed (stamping a renewal instant). {@code level} is
 * deliberately private with no getter - no caller needs the raw level,
 * only its effect via {@link #discountRate()}. {@code renewedAt} DOES have
 * a getter, because a renewal timestamp is genuinely something a caller
 * might need - the difference is what a real caller actually observes.
 */
public class Membership {

    private MembershipLevel level = MembershipLevel.STANDARD;
    private Instant renewedAt;

    public void upgrade() {
        level = MembershipLevel.PREMIUM;
    }

    public BigDecimal discountRate() {
        return level == MembershipLevel.PREMIUM ? new BigDecimal("0.15") : BigDecimal.ZERO;
    }

    /**
     * Records the renewal instant using the given clock rather than
     * calling {@code Clock.systemDefaultZone()} directly - the clock is an
     * explicit, injectable dependency, not an ambient call.
     */
    public void renew(Clock clock) {
        this.renewedAt = Instant.now(clock);
    }

    public Instant getRenewedAt() {
        return renewedAt;
    }
}
