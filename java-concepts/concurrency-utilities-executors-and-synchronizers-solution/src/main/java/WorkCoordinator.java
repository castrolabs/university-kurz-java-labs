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

    public void arriveAtStartupGate() {
        startupGate.countDown();
    }

    public void awaitStartupGate() throws InterruptedException {
        startupGate.await();
    }

    public void awaitRoundBarrier() throws InterruptedException, BrokenBarrierException {
        roundBarrier.await();
    }

    public void runWithPermit(Runnable task) throws InterruptedException {
        resourcePermits.acquire();
        try {
            task.run();
        } finally {
            resourcePermits.release();
        }
    }

    public int incrementUnderLock() {
        lock.lock();
        try {
            return counter.incrementAndGet();
        } finally {
            lock.unlock();
        }
    }

    public int reentrantDoubleIncrement() {
        lock.lock();
        try {
            incrementUnderLock();
            return incrementUnderLock();
        } finally {
            lock.unlock();
        }
    }
}
