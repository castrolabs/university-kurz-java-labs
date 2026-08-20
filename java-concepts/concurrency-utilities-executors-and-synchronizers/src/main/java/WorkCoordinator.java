import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class WorkCoordinator {

    private final CountDownLatch startupGate;
    private final CyclicBarrier roundBarrier;
    private final Semaphore resourcePermits;
    private final ReentrantLock lock = new ReentrantLock();
    private final AtomicInteger counter = new AtomicInteger();

    public WorkCoordinator(int participantCount, int permits) {
        this.startupGate = new CountDownLatch(participantCount);
        this.roundBarrier = new CyclicBarrier(participantCount);
        this.resourcePermits = new Semaphore(permits);
    }

    /**
     * Called by a participant thread once it is ready. The gate opens only
     * after every participant has called this exactly once.
     */
    public void arriveAtStartupGate() {
        // TODO-00: Signal that one participant has arrived. A CountDownLatch
        // is a one-shot counter: use CountDownLatch.countDown().
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    /**
     * Blocks the caller until every participant has called arriveAtStartupGate().
     * Once open, this gate can never be reused or reset.
     */
    public void awaitStartupGate() throws InterruptedException {
        // TODO-01: Block until the startup gate's count reaches zero, using
        // CountDownLatch.await().
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    /**
     * Called once per round by each of `participantCount` participant
     * threads. Unlike the startup gate, this rendezvous point resets itself
     * automatically and can be awaited again on the next round.
     */
    public void awaitRoundBarrier() throws InterruptedException, BrokenBarrierException {
        // TODO-02: Block until `participantCount` threads have all called
        // this method, then let them all resume together, using
        // CyclicBarrier.await(). Unlike CountDownLatch, this same barrier
        // instance is meant to be awaited again on the next round.
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    /**
     * Runs `task` while holding one of `permits` available permits. If more
     * than `permits` callers try to run at once, the extra callers block
     * until a permit is released.
     */
    public void runWithPermit(Runnable task) throws InterruptedException {
        // TODO-03: Acquire a permit (Semaphore.acquire()), run `task`, and
        // release the permit afterwards. The release MUST happen even if
        // `task` throws - put it in a finally block, or a task that throws
        // permanently leaks a permit and every future caller blocks forever.
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    /**
     * Increments the shared counter once while holding `lock`, and returns
     * the new value.
     */
    public int incrementUnderLock() {
        // TODO-04: Acquire `lock`, increment `counter`, release `lock` in a
        // finally block, and return the new value.
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    /**
     * Increments the shared counter twice, by acquiring `lock` and then
     * calling incrementUnderLock() (which acquires the SAME lock again, on
     * the same thread) twice before releasing.
     */
    public int reentrantDoubleIncrement() {
        // TODO-05 (optional): Acquire `lock`, then call incrementUnderLock()
        // twice, then release `lock` in a finally block, returning the
        // second call's result. A ReentrantLock allows a thread that already
        // holds it to acquire it again without deadlocking against itself -
        // that's the behavior this method is meant to demonstrate.
        throw new UnsupportedOperationException("Not implemented yet.");
    }
}
