package com.kurz.dbtransactions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unlike TransferServiceTest, this class IS annotated @Transactional at
 * the class level: Spring wraps every test method in its own transaction
 * and rolls it back automatically once the method returns. That's what
 * lets a test insert a row with no @AfterEach cleanup at all - the row
 * never survives past the test that created it, so it can never leak into
 * a later test.
 */
@SpringBootTest
@Transactional
class TransactionRollbackTest {

    @Autowired
    private AccountRepository accountRepository;

    @Test
    @DisplayName("a row inserted here is visible within the same test")
    void insertedRowIsVisibleWithinTest() {
        accountRepository.save(new Account("Temp Rollback Check", 100, false));

        Optional<Account> found = accountRepository.findByOwner("Temp Rollback Check");

        assertTrue(found.isPresent());
    }

    @Test
    @DisplayName("no row leaks in from another test in this class")
    void noRowLeaksFromAnotherTest() {
        Optional<Account> found = accountRepository.findByOwner("Temp Rollback Check");

        assertTrue(found.isEmpty());
    }

    @Test
    @DisplayName("bonus: flagForCommit() forces a row to survive a real commit")
    void flagForCommitMakesTheRowSurviveACommit() {
        accountRepository.save(new Account("Temp Commit Check", 100, false));

        TestTransaction.flagForCommit();
        TestTransaction.end();

        assertTrue(accountRepository.findByOwner("Temp Commit Check").isPresent());

        accountRepository.findByOwner("Temp Commit Check")
                .ifPresent(account -> accountRepository.deleteById(account.getId()));
    }
}
