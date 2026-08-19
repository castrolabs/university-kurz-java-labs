import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TransferService")
class TransferServiceTest {

    private final TransferService service = new TransferService();

    @FunctionalInterface
    private interface TransferOperation {
        void transfer(Account from, Account to, int amount);
    }

    @Test
    @DisplayName("should move the amount from the source account to the destination account")
    void shouldMoveTheAmountFromTheSourceAccountToTheDestinationAccount() {
        Account alice = new Account(1, 100);
        Account bob = new Account(2, 50);

        service.transfer(alice, bob, 30);

        assertEquals(70, alice.getBalance());
        assertEquals(80, bob.getBalance());
    }

    @Test
    @DisplayName("should conserve the total balance across both accounts")
    void shouldConserveTheTotalBalanceAcrossBothAccounts() {
        Account alice = new Account(1, 100);
        Account bob = new Account(2, 50);

        service.transfer(alice, bob, 30);

        assertEquals(150, alice.getBalance() + bob.getBalance());
    }

    @Test
    @DisplayName("should leave the balance unchanged when transferring to the same account")
    void shouldLeaveTheBalanceUnchangedWhenTransferringToTheSameAccount() {
        Account alice = new Account(1, 100);

        service.transfer(alice, alice, 30);

        assertEquals(100, alice.getBalance());
    }

    @Test
    @DisplayName("should complete many concurrent bidirectional transfers without deadlocking")
    void shouldCompleteManyConcurrentBidirectionalTransfersWithoutDeadlocking() throws InterruptedException {
        assertCompletesWithoutDeadlock(service::transfer, 40, 25);
    }

    @Test
    @DisplayName("should transfer correctly using identity-hash lock ordering (optional)")
    void shouldTransferCorrectlyUsingIdentityHashLockOrderingOptional() {
        Account alice = new Account(1, 100);
        Account bob = new Account(2, 50);

        service.transferUsingIdentityHash(alice, bob, 30);

        assertEquals(70, alice.getBalance());
        assertEquals(80, bob.getBalance());
    }

    @Test
    @DisplayName("should complete many concurrent bidirectional transfers using identity-hash ordering without deadlocking (optional)")
    void shouldCompleteManyConcurrentBidirectionalTransfersUsingIdentityHashWithoutDeadlockingOptional()
            throws InterruptedException {
        assertCompletesWithoutDeadlock(service::transferUsingIdentityHash, 40, 25);
    }

    private void assertCompletesWithoutDeadlock(TransferOperation operation, int pairs, int iterationsPerThread)
            throws InterruptedException {
        Account alice = new Account(1, 1_000_000);
        Account bob = new Account(2, 1_000_000);
        int threadCount = pairs * 2;

        CountDownLatch ready = new CountDownLatch(threadCount);
        CountDownLatch start = new CountDownLatch(1);
        List<Callable<Void>> tasks = new ArrayList<>();

        for (int i = 0; i < pairs; i++) {
            tasks.add(() -> {
                ready.countDown();
                start.await();
                for (int j = 0; j < iterationsPerThread; j++) {
                    operation.transfer(alice, bob, 1);
                }
                return null;
            });
            tasks.add(() -> {
                ready.countDown();
                start.await();
                for (int j = 0; j < iterationsPerThread; j++) {
                    operation.transfer(bob, alice, 1);
                }
                return null;
            });
        }

        Thread starter = new Thread(() -> {
            try {
                ready.await();
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
            start.countDown();
        });
        starter.setDaemon(true);
        starter.start();

        ExecutorService pool = Executors.newFixedThreadPool(threadCount, runnable -> {
            Thread thread = new Thread(runnable);
            thread.setDaemon(true);
            return thread;
        });

        List<Future<Void>> futures = pool.invokeAll(tasks, 5, TimeUnit.SECONDS);
        pool.shutdownNow();

        for (Future<Void> future : futures) {
            try {
                future.get();
            } catch (CancellationException e) {
                fail("a transfer never completed within the timeout - the locks likely deadlocked");
            } catch (ExecutionException e) {
                Throwable cause = e.getCause();
                if (cause instanceof RuntimeException runtimeException) {
                    throw runtimeException;
                }
                throw new RuntimeException(cause);
            }
        }

        assertEquals(2_000_000, alice.getBalance() + bob.getBalance(),
                "the total balance across both accounts must be conserved");
    }
}
