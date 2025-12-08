package Thread;


import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class MyThreadPool {
    private static final int RUNNING = 0; // 运行中
    private static final int SHUTDOWN = 1; // 已关闭，不再接受新任务，会执行已经提交的任务
    private static final int STOP = 2; // 停止，不再接受新任务，中断正在执行的任务
    private static final int TERMINATED = 3; // 终止，所有任务已完成

    private final int corePoolSize;
    private final int maximumPoolSize;
    private final long keepAliveTime;
    private final TimeUnit unit;
    private final BlockingQueue<Runnable> workQueue;
    private final ThreadFactory threadFactory;
    private final RejectedExecutionHandler handler;

    private final Set<Worker> workers = new HashSet<>(); // 工作线程集合
    private volatile int runState = RUNNING; // 线程池状态
    private final AtomicInteger completedTaskCount = new AtomicInteger(); // 已完成任务数
    private final AtomicInteger taskCount = new AtomicInteger(); // 总任务数

    /*
    拒绝策略接口
     */
    @FunctionalInterface
    public interface RejectedExecutionHandler {
        void rejectedExecution(Runnable r, MyThreadPool executor);
    }

    /*
    工作线程类，负责执行任务
     */
    private final class Worker implements Runnable {
        final Thread thread;
        Runnable firstTask;
        volatile long completedTasks;

        public Worker(Runnable firstTask) {
            this.firstTask = firstTask;
            this.thread = threadFactory.newThread(this);
        }

        @Override
        public void run() {
            runWorker(this);
        }
    }

    /*
    构造函数，初始化线程池参数
     */
    public MyThreadPool(int corePoolSize,
                        int maximumPoolSize,
                        long keepAliveTime,
                        TimeUnit unit,
                        BlockingQueue<Runnable> workQueue,
                        ThreadFactory threadFactory,
                        RejectedExecutionHandler handler) {
        if (corePoolSize < 0 ||
                maximumPoolSize <= 0 ||
                maximumPoolSize < corePoolSize ||
                keepAliveTime < 0) {
            throw new IllegalArgumentException();
        }
        if (workQueue == null || threadFactory == null || handler == null) {
            throw new NullPointerException();
        }
        this.corePoolSize = corePoolSize;
        this.maximumPoolSize = maximumPoolSize;
        this.workQueue = workQueue;
        this.keepAliveTime = keepAliveTime;
        this.unit = unit;
        this.handler = handler;
        this.threadFactory = threadFactory;
    }

    /*
    检查线程池是否处于运行状态
     */
    private boolean isRunning(int state) {
        return state == RUNNING;
    }

    /*
    添加工作线程
     */
    private boolean addWorker(Runnable firstTask, boolean core) {
        retry:
        for (; ; ) {
            int c = runState;
            if (c >= SHUTDOWN) {
                if (c == SHUTDOWN && firstTask == null) {
                    //线程池已关闭，但允许添加空任务线程来处理剩余任务
                } else {
                    return false;
                }
            }
            for (; ; ) {
                // 获取工作线程数量
                int wc = workers.size();
                // 如果工作线程数量大于等于（核心线程数量：最大线程数量）
                if (wc >= (core ? corePoolSize : maximumPoolSize)) {
                    return false;
                }
                // CAS原子递增任务计数（CAS确保并发安全）
                if (taskCount.compareAndSet(c, c + 1)) {
                    break retry;
                }
                //CAS失败，重新检查
                c = runState;
                if (c != RUNNING) {
                    continue retry;
                }
            }
        }

        boolean workerStarted = false;
        boolean workerAdded = false;
        Worker w = null;
        try {
            // 创建Worker实例
            w = new Worker(firstTask);
            final Thread t = w.thread;
            if (t != null) {
                synchronized (workers) {
                    int c = runState;
                    if (isRunning(c) || (c == SHUTDOWN && firstTask == null)) {
                        if (t.isAlive()) {
                            throw new IllegalThreadStateException();
                        }
                        workers.add(w);
                        workerAdded = true;
                    }
                }
                if (workerAdded) {
                    t.start();
                    workerStarted = true;
                }
            }
        } finally {
            if (!workerStarted) {
                if (workerAdded) {
                    synchronized (workers) {
                        workers.remove(w);
                    }
                }
                taskCount.decrementAndGet();
            }
        }
        return workerStarted;
    }

    /*
    工作线程执行任务核心方法
     */
    private void runWorker(Worker w) {
        Thread wt = Thread.currentThread();
        Runnable task = w.firstTask;
        w.firstTask = null;
        while (task != null || (task = getTask()) != null) {
            try {
                task.run();
            } catch (Throwable x) {
                System.err.println("Exception in task " + x.getMessage());
            } finally {
                w.completedTasks++;
                completedTaskCount.incrementAndGet();
                task = null;
            }
        }
        processWorkerExit(w, false);
    }

    /*
    从任务队列获取任务
     */
    private Runnable getTask() {
        boolean timedOut = false;
        for (; ; ) {
            int c = runState;
            if (c >= SHUTDOWN && (c >= STOP || workQueue.isEmpty())) {
                taskCount.decrementAndGet();
                return null;
            }
            int wc = workers.size();
            boolean timed = wc > corePoolSize;
            if ((wc > maximumPoolSize || (timed && timedOut)) && (wc > 1 || workQueue.isEmpty())) {
                if (taskCount.compareAndSet(c, c - 1)) {
                    return null;
                }
                continue;
            }
            try {
                Runnable r = timed ?
                        workQueue.poll(keepAliveTime, TimeUnit.NANOSECONDS) :
                        workQueue.take();
                if (r != null) {
                    return r;
                }
                timedOut = true;
            } catch (InterruptedException retry) {
                timedOut = false;
            }
        }
    }

    /*
    处理工作线程退出
     */
    private void processWorkerExit(Worker w, boolean completedAbruptly) {
        if (completedAbruptly) {
            taskCount.decrementAndGet();
        }
        synchronized (workers) {
            workers.remove(w);
        }
        tryTerminate();
        int c = runState;
        if (c == RUNNING || c == SHUTDOWN) {
            if (!completedAbruptly) {
                int min = corePoolSize;
                if (min == 0 && !workQueue.isEmpty()) {
                    min = 1;
                }
                if (workers.size() >= min) {
                    return;
                }
            }
            addWorker(null, false);
        }
    }

    /*
    尝试终止线程池
     */
    private void tryTerminate() {
        for (; ; ) {
            int c = runState;
            if (isRunning(c) ||
                    c == TERMINATED ||
                    (c == SHUTDOWN && !workQueue.isEmpty())) {
                return;
            }
            if (workers.size() > 0) {
                interruptIdleWorkers();
                return;
            }
            synchronized (this) {
                if (runState == SHUTDOWN) {
                    runState = TERMINATED;
                    return;
                }
            }
        }
    }

    /*
    中断空闲的工作线程
     */
    private void interruptIdleWorkers() {
        synchronized (workers) {
            for (Worker w : workers) {
                Thread t = w.thread;
                if (!t.isInterrupted()) {
                    t.interrupt();
                }
            }
        }
    }

    /*
    提交任务到线程池
     */
    public void execute(Runnable command) {
        if (command == null) {
            throw new NullPointerException();
        }

        int c = taskCount.get();
        if (isRunning(runState) && workers.size() < corePoolSize) {
            if (addWorker(command, true)) {
                return;
            }
            c = taskCount.get();
        }
        if (isRunning(runState) && workQueue.offer(command)) {
            int recheck = taskCount.incrementAndGet();
            if (!isRunning(recheck) && remove(command)) {
                handler.rejectedExecution(command, this);
            } else if (workers.size() == 0) {
                addWorker(null, false);
            }
        } else if (!addWorker(command, false)) {
            handler.rejectedExecution(command, this);
        }
    }

    /*
    关闭线程池，不再接受新任务，但会执行已提交的任务
     */
    public void shutdown() {
        synchronized (this) {
            if (runState != RUNNING) {
                return;
            }
            runState = SHUTDOWN; //改状态
            interruptIdleWorkers(); //中断空闲线程
        }
        tryTerminate(); //尝试终止线程池
    }

    /*
    立即关闭线程池，尝试中断正在执行的任务，并返回未执行的任务
     */
    public List<Runnable> shutdownNow() {
        List<Runnable> tasks = new ArrayList<>();
        synchronized (this) {
            if (runState != RUNNING && runState != SHUTDOWN) {
                return tasks;
            }
            runState = STOP;
            interruptIdleWorkers();
            workQueue.drainTo(tasks);
        }
        tryTerminate();
        return tasks;
    }
    /*
    从队列中移除任务
     */
    private boolean remove(Runnable task) {
        boolean removed = workQueue.remove(task);
        if (removed) {
            taskCount.decrementAndGet();
        }
        return removed;
    }
    /*
    获取已完成的任务数
     */
    public int getCompletedTaskCount() {
        return completedTaskCount.get();
    }
    /*
    获取当前活跃的线程数
     */
    public int getActiveCount() {
        synchronized (workers) {
            return workers.size();
        }
    }

    public int getQueueSize() {
        return workQueue.size();
    }
    /*
    等待线程池终止
     */
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        long nanos = unit.toNanos(timeout);
        long lastTime = System.nanoTime();
        while (runState!= TERMINATED) {
            if (nanos <= 0) {
                return false;
            }
            TimeUnit.NANOSECONDS.sleep(Math.min(nanos, 1000000));
            long now = System.nanoTime();
            nanos -= now - lastTime;
            lastTime = now;
        }
        return true;
    }

    /**
     * 直接抛出异常的拒绝策略
     */
    public static class AbortPolicy implements RejectedExecutionHandler {
        @Override
        public void rejectedExecution(Runnable r, MyThreadPool executor) {
            throw new RejectedExecutionException("任务 " + r + " 被拒绝，线程池已达承载上限");
        }
    }

    /**
     * 让提交任务的线程执行任务的拒绝策略
     */
    public static class CallerRunsPolicy implements RejectedExecutionHandler {
        @Override
        public void rejectedExecution(Runnable r, MyThreadPool executor) {
            if (!executor.isRunning(executor.runState)) {
                r.run();
            }
        }
    }

    /**
     * 丢弃最旧任务的拒绝策略
     */
    public static class DiscardOldestPolicy implements RejectedExecutionHandler {
        @Override
        public void rejectedExecution(Runnable r, MyThreadPool executor) {
            if (!executor.isRunning(executor.runState)) {
                Runnable oldest = executor.workQueue.poll();
                if (oldest != null) {
                    executor.taskCount.decrementAndGet();
                }
                executor.workQueue.offer(r);
                executor.taskCount.incrementAndGet();
            }
        }
    }
    /**
     *
     */
    public static class DefaultThreadFactory implements ThreadFactory {
        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;
        public DefaultThreadFactory() {
            SecurityManager s = System.getSecurityManager();
            group = (s!=null)?s.getThreadGroup():
                    Thread.currentThread().getThreadGroup();
            namePrefix = "custom-pool-"+
                    poolNumber.getAndIncrement()+"-thread-";
        }
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r,
                    namePrefix + threadNumber.getAndIncrement(),
                    0);
            if (t.isDaemon()) {
                t.setDaemon(false);
            }
            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
            }
            return t;
        }
    }

}
