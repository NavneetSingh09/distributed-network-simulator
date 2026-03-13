package sim.client;

import sim.config.Ports;
import sim.model.Packet;
import sim.osi.OsiStack;
import sim.util.Log;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.UUID;

public class TrafficSimulator {

    private static final int CLIENT_COUNT = 50;

    public void start() {

        Log.info("SIMULATOR", "Starting traffic simulation with " + CLIENT_COUNT + " clients");

        for (int i = 0; i < CLIENT_COUNT; i++) {

            int clientId = i;

            new Thread(() -> simulateClient(clientId)).start();
        }
    }

    private void simulateClient(int clientId) {

        try {

            OsiStack stack = new OsiStack();

            String message = "Hello from client-" + clientId;

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

            Log.info("CLIENT-" + clientId,
                    "Packet sent: " + packet.getPacketId());

            socket.close();

        } catch (Exception e) {

            Log.error("CLIENT-" + clientId, "Failed to send packet");
        }
    }
}