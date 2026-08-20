package com.kurz.dbtransactions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.fail;

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

    // TODO-02: Save a new Account (owner "Temp Rollback Check", any
    // balance, not closed) via accountRepository, then assert
    // accountRepository.findByOwner("Temp Rollback Check") is present -
    // proving the insert is visible from within the same test.
    @Test
    @DisplayName("a row inserted here is visible within the same test")
    void insertedRowIsVisibleWithinTest() {
        fail("TODO-02: not implemented yet");
    }

    // TODO-03: Assert accountRepository.findByOwner("Temp Rollback
    // Check") is EMPTY here - proving that whatever
    // insertedRowIsVisibleWithinTest() inserted was rolled back once that
    // test method returned, even though neither test called any explicit
    // delete or cleanup method. This works regardless of which of the two
    // tests JUnit happens to run first: by the time either test body
    // starts, any previous test's transaction has already been rolled
    // back.
    @Test
    @DisplayName("no row leaks in from another test in this class")
    void noRowLeaksFromAnotherTest() {
        fail("TODO-03: not implemented yet");
    }

    // TODO-04 (optional): Save an Account with owner "Temp Commit Check",
    // then call org.springframework.test.context.transaction.TestTransaction.flagForCommit()
    // followed by TestTransaction.end() to force this test's transaction
    // to actually commit right now, instead of waiting to roll back at
    // the end of the test. Assert accountRepository.findByOwner("Temp
    // Commit Check") is still present after that (proving the row
    // survived a real commit), then delete it yourself with
    // accountRepository - since flagForCommit() defeats the class-level
    // @Transactional's automatic cleanup for this row, the framework will
    // no longer roll it back for you.
}
