package com.kurz.dbtransactions;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.fail;

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

    // TODO-00: Call transferService.transfer(checkingId, closedId, 5_000)
    // and assert it throws IllegalStateException. Then reload the
    // checking account with accountRepository.findById(checkingId) and
    // assert its balance is still 10_000 - the debit made earlier in the
    // SAME @Transactional method call must have been rolled back along
    // with everything else, because the destination account turned out to
    // be closed.
    @Test
    @DisplayName("a transfer to a closed account rolls back the debit too")
    void failedTransferRollsBackTheDebit() {
        fail("TODO-00: not implemented yet");
    }

    // TODO-01: Save a second, OPEN destination account (e.g. new
    // Account("Savings", 0, false)) via accountRepository. Call
    // transferService.transfer(checkingId, <that account's id>, 3_000) and
    // assert it does NOT throw. Reload both accounts and assert the
    // balances actually moved: checking is now 7_000, and the destination
    // account's balance is now 3_000.
    @Test
    @DisplayName("a successful transfer commits both the debit and the credit")
    void successfulTransferCommitsBothSides() {
        fail("TODO-01: not implemented yet");
    }
}
