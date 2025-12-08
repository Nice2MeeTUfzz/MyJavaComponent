package Thread;

/**
 * 使用Synchronized实现线程安全计数器
 */
class SynchronizedCounter {
    // 使用volatile修饰counter
    private volatile int counter = 0;

    //    增加操作加锁
    public synchronized void increment() {
//        ++非原子操作
        counter++;
    }

    public int get() {
        return counter;
    }

    public static void main(String[] args) throws InterruptedException {
        SynchronizedCounter counter = new SynchronizedCounter();
        Thread[] threads = new Thread[10];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 1000; j++) {
                    counter.increment();
                }
            });
            threads[i].start();
        }

        for (Thread t : threads) {
            t.join();
        }

        System.out.println("Final count: " + counter.get());
    }
}