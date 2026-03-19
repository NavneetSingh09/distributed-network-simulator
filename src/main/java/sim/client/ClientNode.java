package sim.client;

import sim.config.Ports;
import sim.model.Packet;
import sim.util.Log;
import sim.metrics.MetricsStore;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ClientNode {

    private String sourceIp;
    private String destinationIp;

    public ClientNode(String sourceIp, String destinationIp) {
        this.sourceIp = sourceIp;
        this.destinationIp = destinationIp;
    }

    public void start() {

        Scanner scanner = new Scanner(System.in);

        Log.info("CLIENT", "Client started");

        while (true) {

            System.out.print("Enter message (or type 'auto'): ");

            String message = scanner.nextLine();

            // 🔥 AUTO MODE
            if (message.equalsIgnoreCase("auto")) {

                Log.info("CLIENT", "Switching to AUTO traffic mode...");

                runAutoMode();   // start continuous traffic
                break;
            }

            // 🔹 MANUAL MODE
            try {

                Packet packet = Packet.newRequest(
                        sourceIp,
                        destinationIp,
                        message
                );

                sendPacket(packet);

            } catch (Exception e) {

                Log.error("CLIENT", "Failed to send packet");

            }
        }
    }

    // 🔥 AUTO TRAFFIC GENERATOR
   private void runAutoMode() {

    try {

        while (true) {

            // 🔥 bigger random burst (1–10)
            int burst = 1 + (int)(Math.random() * 10);

            for (int i = 0; i < burst; i++) {

                String message = "AutoMsg-" + System.currentTimeMillis();

                Packet packet = Packet.newRequest(
                        sourceIp,
                        destinationIp,
                        message
                );

                sendPacket(packet);
            }

            // smaller delay (continuous traffic)
            int delay = 100 + (int)(Math.random() * 200); // 100–300ms

            Thread.sleep(delay);
        }

    } catch (Exception e) {

        Log.error("CLIENT", "Auto mode crashed: " + e.getMessage());

    }
}

    // 🔁 COMMON SEND METHOD (reusable)
    private void sendPacket(Packet packet) {

        try {

            Socket socket = new Socket("localhost", Ports.ROUTER_PORT);

            PrintWriter writer =
                    new PrintWriter(socket.getOutputStream(), true);

            writer.println(packet.serialize());

            socket.close();

            MetricsStore.packetSent();

            Log.info("CLIENT", "Packet sent: " + packet.getPacketId());

        } catch (Exception e) {

            Log.error("CLIENT", "Error sending packet: " + packet.getPacketId());

        }
    }
}