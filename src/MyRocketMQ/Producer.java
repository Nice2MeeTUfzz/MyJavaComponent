package MyRocketMQ;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Producer {
    private final List<MessageQueue> queues;

    public Producer(List<MessageQueue> queues) {
        this.queues = queues;
    }

    public void send(long orderId, String content) throws InterruptedException {
        int queueIndex = (int) (orderId % queues.size());
        MessageQueue targetQueue = queues.get(queueIndex);
        Message msg = new Message(orderId, content);
        targetQueue.put(msg);
        System.out.println("[Producer] sent message: " + msg + " → Queue-" + targetQueue.getId());
    }
}
