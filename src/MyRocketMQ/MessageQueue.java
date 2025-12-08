package MyRocketMQ;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class MessageQueue {
    private final int id;
    private final BlockingQueue<Message> queue = new LinkedBlockingDeque<>();

    public MessageQueue(int id) {
        this.id = id;
    }

    public void put(Message message) throws InterruptedException {
        queue.put(message);
    }

    public Message take() throws InterruptedException {
        return queue.take();
    }

    public int getId() {
        return id;
    }
}
