import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Account")
class AccountTest {

    @Test
    @DisplayName("should decrease the balance when debited")
    void shouldDecreaseTheBalanceWhenDebited() {
        Account account = new Account(1, 100);

        account.debit(30);

        assertEquals(70, account.getBalance());
    }

    @Test
    @DisplayName("should increase the balance when credited")
    void shouldIncreaseTheBalanceWhenCredited() {
        Account account = new Account(1, 100);

        account.credit(30);

        assertEquals(130, account.getBalance());
    }
}
