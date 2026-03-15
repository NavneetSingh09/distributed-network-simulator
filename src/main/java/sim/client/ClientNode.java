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

            System.out.print("Enter message: ");

            String message = scanner.nextLine();

            try {

                Packet packet = Packet.newRequest(
                        sourceIp,
                        destinationIp,
                        message
                );

                Socket socket = new Socket("localhost", Ports.ROUTER_PORT);

                PrintWriter writer =
                        new PrintWriter(socket.getOutputStream(), true);

                writer.println(packet.serialize());

                socket.close();

                MetricsStore.packetSent();

                Log.info("CLIENT", "Packet sent: " + packet.getPacketId());

            } catch (Exception e) {

                Log.error("CLIENT", "Failed to send packet");

            }
        }
    }
}