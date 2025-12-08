package MyRocketMQ;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
public class OrderlyConsumer {
    private final List<MessageQueue> queues;
    private final ExecutorService executor = Executors.newFixedThreadPool(4);

    public OrderlyConsumer(List<MessageQueue> queues) {
        this.queues = queues;
    }

    public void start() {
        for (MessageQueue mq : queues) {
            final int qId = mq.getId();
            executor.submit(()->{
                try {
                    while (true) {
                        Message msg = mq.take();
                        Thread.sleep(200);
                        System.out.println("[Consumer][Queue-" + qId + "] Processing: " + msg);
                    }
                }catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
    }

    public void shutdown() {
        executor.shutdown();
    }
}
