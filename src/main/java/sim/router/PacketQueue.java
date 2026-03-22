package sim.router;

import org.springframework.stereotype.Component;
import sim.model.Packet;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;


@Component
public class PacketQueue {

    private static final int CAPACITY = 100;

    private final BlockingQueue<Packet> queue = new LinkedBlockingQueue<>(CAPACITY);

    
    public boolean enqueue(Packet packet) {
        return queue.offer(packet);
    }

    
    public Packet dequeue(long timeoutMs) throws InterruptedException {
        return queue.poll(timeoutMs, TimeUnit.MILLISECONDS);
    }

    
    public int size() {
        return queue.size();
    }

   
    public boolean isFull() {
        return queue.remainingCapacity() == 0;
    }
}