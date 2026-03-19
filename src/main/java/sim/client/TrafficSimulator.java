package sim.client;

import sim.config.Ports;
import sim.model.Packet;
import sim.osi.OsiStack;
import sim.util.Log;
import sim.metrics.MetricsStore;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.Random;

public class TrafficSimulator {

    private Random random = new Random();

    /**
     * Sends ONE packet (called repeatedly by SimulatorService)
     */
    public void sendPacket() {

        int clientId = random.nextInt(50); // simulate multiple clients

        try {

            OsiStack stack = new OsiStack();

            String message = "Hello from client-" + clientId + 
                             " | " + System.currentTimeMillis();

            String encoded = stack.encapsulate(message);

            Packet packet = Packet.newRequest(
                    "10.0.0." + clientId,
                    "10.0.0.100",
                    encoded
            );

            Socket socket = new Socket("localhost", Ports.ROUTER_PORT);

            PrintWriter writer =
                    new PrintWriter(socket.getOutputStream(), true);

            writer.println(packet.serialize());

            socket.close();

            MetricsStore.packetSent();

            Log.info("TRAFFIC",
                    "Packet sent: " + packet.getPacketId());

        } catch (Exception e) {

            Log.error("TRAFFIC", "Failed to send packet");

        }
    }
}