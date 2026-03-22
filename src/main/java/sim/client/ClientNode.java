package sim.client;

import sim.config.Ports;
import sim.metrics.MetricsStore;
import sim.model.Packet;
import sim.util.Log;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;


public class ClientNode {

    private final String       sourceIp;
    private final String       destinationIp;
    private final MetricsStore metricsStore;

    public ClientNode(String sourceIp, String destinationIp, MetricsStore metricsStore) {
        this.sourceIp      = sourceIp;
        this.destinationIp = destinationIp;
        this.metricsStore  = metricsStore;
    }

    public void start() {
        Scanner scanner = new Scanner(System.in);
        Log.info("CLIENT", "Client started");

        while (true) {
            System.out.print("Enter message (or type 'auto'): ");
            String message = scanner.nextLine();

            if (message.equalsIgnoreCase("auto")) {
                Log.info("CLIENT", "Switching to AUTO traffic mode...");
                runAutoMode();
                break;
            }

            try {
                Packet packet = Packet.newRequest(sourceIp, destinationIp, message);
                sendPacket(packet);
            } catch (Exception e) {
                Log.error("CLIENT", "Failed to send packet");
            }
        }
    }

    private void runAutoMode() {
        try {
            while (true) {
                int burst = 1 + (int) (Math.random() * 10);
                for (int i = 0; i < burst; i++) {
                    String msg    = "AutoMsg-" + System.currentTimeMillis();
                    Packet packet = Packet.newRequest(sourceIp, destinationIp, msg);
                    sendPacket(packet);
                }
                int delay = 100 + (int) (Math.random() * 200);
                Thread.sleep(delay);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            Log.error("CLIENT", "Auto mode crashed: " + e.getMessage());
        }
    }

    private void sendPacket(Packet packet) {
        try (Socket socket = new Socket("localhost", Ports.ROUTER_PORT);
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {

            writer.println(packet.serialize());
            metricsStore.packetSent();
            Log.info("CLIENT", "Packet sent: " + packet.getPacketId());

        } catch (Exception e) {
            Log.error("CLIENT", "Error sending packet: " + packet.getPacketId());
        }
    }
}
