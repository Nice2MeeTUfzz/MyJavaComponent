package MyRocketMQ;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Message {

    private final long orderId;
    private final String content;
    private final long timestamp;

    public long getOrderId() {
        return orderId;
    }

    public String getContent() {
        return content;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Message(long orderId, String content) {
        this.orderId = orderId;
        this.content = content;
        this.timestamp = System.currentTimeMillis();
    }

    @Override
    public String toString() {
        return String.format("[OrderId=%d, Content=%s, Time=%d]", orderId, content, timestamp);
    }
}
