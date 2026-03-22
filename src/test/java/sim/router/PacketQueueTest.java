package sim.router;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sim.model.Packet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PacketQueue bounded blocking queue.
 */
class PacketQueueTest {

    private PacketQueue queue;

    @BeforeEach
    void setUp() {
        queue = new PacketQueue();
    }

    private Packet makePacket() {
        return Packet.newRequest("10.0.0.1", "10.0.0.2", "test");
    }

    @Test
    void enqueue_singlePacket_succeeds() {
        assertTrue(queue.enqueue(makePacket()));
        assertEquals(1, queue.size());
    }

    @Test
    void dequeue_returnsEnqueuedPacket() throws InterruptedException {
        Packet p = makePacket();
        queue.enqueue(p);
        Packet result = queue.dequeue(500);
        assertNotNull(result);
        assertEquals(p.getPacketId(), result.getPacketId());
    }

    @Test
    void dequeue_emptyQueue_returnsNull() throws InterruptedException {
        Packet result = queue.dequeue(100);
        assertNull(result);
    }

    @Test
    void isFull_returnsFalse_whenEmpty() {
        assertFalse(queue.isFull());
    }

    @Test
    void enqueue_beyondCapacity_returnsFalse() {
        // Fill the queue to capacity (100)
        for (int i = 0; i < 100; i++) {
            assertTrue(queue.enqueue(makePacket()));
        }
        // 101st should be rejected
        assertFalse(queue.enqueue(makePacket()));
        assertTrue(queue.isFull());
    }

    @Test
    void size_reflectsCurrentQueueDepth() {
        queue.enqueue(makePacket());
        queue.enqueue(makePacket());
        queue.enqueue(makePacket());
        assertEquals(3, queue.size());
    }
}