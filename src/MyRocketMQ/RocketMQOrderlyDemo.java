package MyRocketMQ;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
public class RocketMQOrderlyDemo {
    public static void main(String[] args) throws InterruptedException {
        List<MessageQueue> queues = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            queues.add(new MessageQueue(i));
        }
        Producer producer = new Producer(queues);
        OrderlyConsumer consumer = new OrderlyConsumer(queues);
        consumer.start();

        // 模拟发送消息（乱序发送，但同 orderId 消息路由到同一队列）
        // 订单 1001：创建 → 支付 → 发货
        producer.send(1001, "Order Created");
        producer.send(1002, "Order Created"); // 不同订单，可并发
        Thread.sleep(100);
        producer.send(1001, "Payment Success");
        producer.send(1003, "Order Created");
        Thread.sleep(100);
        producer.send(1001, "Shipped");
        producer.send(1002, "Payment Success");

        Thread.sleep(3000);
        consumer.shutdown();
    }
}
