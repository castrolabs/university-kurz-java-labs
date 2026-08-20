package com.kurz.dbtransactions;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * This class is deliberately NOT annotated @Transactional. If it were,
 * Spring's test framework would already have an open transaction running
 * before transferService.transfer(...) is called, and since transfer() is
 * @Transactional with the default REQUIRED propagation, it would simply
 * JOIN that already-open transaction instead of starting its own. A
 * failed transfer would then mark the SHARED transaction rollback-only,
 * but the physical ROLLBACK wouldn't happen until the test method itself
 * returns - so re-querying a balance inside that same still-open
 * transaction could still see the (uncommitted, not-yet-rolled-back)
 * debit, and a test written that way could pass or fail for the wrong
 * reason depending on caching, not because rollback did or didn't happen.
 *
 * Keeping this class non-transactional lets transfer() run in its own
 * real, standalone transaction, so "the balance really did roll back" is
 * an actually-proven fact instead of a coincidence. Manual cleanup in
 * tearDown() is the cost of that: nothing here rolls back automatically.
 */
@SpringBootTest
class TransferServiceTest {

    @Autowired
    private TransferService transferService;

    @Autowired
    private AccountRepository accountRepository;

    private Long checkingId;
    private Long closedId;

    @BeforeEach
    void setUp() {
        checkingId = accountRepository.save(new Account("Checking", 10_000, false)).getId();
        closedId = accountRepository.save(new Account("Closed", 0, true)).getId();
    }

    @AfterEach
    void tearDown() {
        accountRepository.deleteAll();
    }

    @Test
    @DisplayName("a transfer to a closed account rolls back the debit too")
    void failedTransferRollsBackTheDebit() {
        assertThrows(IllegalStateException.class,
                () -> transferService.transfer(checkingId, closedId, 5_000));

        Account checking = accountRepository.findById(checkingId).orElseThrow();
        assertEquals(10_000, checking.getBalanceCents());
    }

    @Test
    @DisplayName("a successful transfer commits both the debit and the credit")
    void successfulTransferCommitsBothSides() {
        Long savingsId = accountRepository.save(new Account("Savings", 0, false)).getId();

        transferService.transfer(checkingId, savingsId, 3_000);

        Account checking = accountRepository.findById(checkingId).orElseThrow();
        Account savings = accountRepository.findById(savingsId).orElseThrow();
        assertEquals(7_000, checking.getBalanceCents());
        assertEquals(3_000, savings.getBalanceCents());
    }
}
