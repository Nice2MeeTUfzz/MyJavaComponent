package Thread;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 线程安全计数器
 */
public class SafeCounter {
    private AtomicInteger counter = new AtomicInteger(0);

    public void increment() {
        counter.incrementAndGet();
    }

    public int getCount() {
        return counter.get();
    }
}
