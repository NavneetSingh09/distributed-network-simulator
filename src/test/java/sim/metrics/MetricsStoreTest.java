package sim.metrics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class MetricsStoreTest {

    private MetricsStore store;

    @BeforeEach
    void setUp() {
        store = new MetricsStore();
    }

    @Test
    void packetSent_incrementsCount() {
        store.packetSent();
        store.packetSent();
        assertEquals(2, store.getPacketsSent());
    }

    @Test
    void packetDropped_incrementsCount() {
        store.packetDropped();
        assertEquals(1, store.getPacketsDropped());
    }

    @Test
    void packetProcessed_incrementsCount() {
        store.packetProcessed();
        assertEquals(1, store.getPacketsProcessed());
    }

    @Test
    void server1Handled_incrementsServer1Load() {
        store.server1Handled();
        store.server1Handled();
        assertEquals(2, store.getServer1Load());
        assertEquals(0, store.getServer2Load());
    }

    @Test
    void server2Handled_incrementsServer2Load() {
        store.server2Handled();
        assertEquals(0, store.getServer1Load());
        assertEquals(1, store.getServer2Load());
    }

    @Test
    void avgLatency_returnsZero_whenNoSamples() {
        assertEquals(0.0, store.avgLatency());
    }

    @Test
    void avgLatency_calculatesCorrectly() {
        store.recordLatency(100);
        store.recordLatency(200);
        assertEquals(150.0, store.avgLatency());
    }

    @Test
    void packetQueued_and_dequeued_trackCorrectly() {
        store.packetQueued();
        store.packetQueued();
        store.packetDequeued();
        assertEquals(2, store.getPacketsQueued());
        assertEquals(1, store.getPacketsDequeued());
    }

    @Test
    void concurrentIncrement_isThreadSafe() throws InterruptedException {
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 1000; i++) store.packetSent();
        });
        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 1000; i++) store.packetSent();
        });

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        assertEquals(2000, store.getPacketsSent());
    }
}