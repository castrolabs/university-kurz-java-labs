import java.util.HashMap;
import java.util.Map;

/**
 * An in-process collaborator: tracks account balances entirely in memory.
 * Nothing outside this application ever talks to a Ledger directly - it's
 * an implementation detail of how {@link TransferService} keeps balances,
 * never something an external client observes.
 */
public class Ledger {

    private final Map<String, Integer> balances = new HashMap<>();

    public void openAccount(String accountId, int startingBalanceCents) {
        balances.put(accountId, startingBalanceCents);
    }

    public boolean hasSufficientFunds(String accountId, int amountCents) {
        return balanceOf(accountId) >= amountCents;
    }

    public void debit(String accountId, int amountCents) {
        balances.merge(accountId, -amountCents, Integer::sum);
    }

    public int balanceOf(String accountId) {
        return balances.getOrDefault(accountId, 0);
    }
}
