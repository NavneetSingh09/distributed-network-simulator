package sim.router;

import sim.model.Packet;
import sim.util.Log;
import sim.config.Ports;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

public class Router {

    private int currentServer = 0;
    private Random random = new Random();

    public void start() {

        try (ServerSocket routerSocket = new ServerSocket(Ports.ROUTER_PORT)) {

            Log.info("ROUTER", "Router started on port " + Ports.ROUTER_PORT);

            while (true) {

                Socket clientSocket = routerSocket.accept();

                BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(clientSocket.getInputStream()));

                String data = reader.readLine();

                if (data == null) {
                    clientSocket.close();
                    continue;
                }

                Packet packet = Packet.deserialize(data);

                Log.info("ROUTER", "Packet received: " + packet.getPacketId());

                // Simulate packet drop (10%)
                if (random.nextDouble() < 0.10) {
                    Log.warn("ROUTER", "Packet dropped: " + packet.getPacketId());
                    clientSocket.close();
                    continue;
                }

                int serverPort = getNextServer();

                // Simulate network latency
                simulateLatency();

                Log.info("ROUTER", "Forwarding packet to server on port " + serverPort);

                forwardToServer(packet, serverPort);

                clientSocket.close();
            }

        } catch (Exception e) {
            Log.error("ROUTER", "Router failure: " + e.getMessage());
        }
    }

    private int getNextServer() {

        currentServer = (currentServer + 1) % 2;

        if (currentServer == 0)
            return Ports.SERVER1_PORT;
        else
            return Ports.SERVER2_PORT;
    }

    private void simulateLatency() {

        try {

            int delay = 50 + random.nextInt(250); // 50–300 ms delay

            Log.info("ROUTER", "Simulating network latency: " + delay + "ms");

            Thread.sleep(delay);

        } catch (Exception ignored) {
        }
    }

    private void forwardToServer(Packet packet, int port) {

        try (Socket serverSocket = new Socket("localhost", port);
             PrintWriter writer =
                     new PrintWriter(serverSocket.getOutputStream(), true)) {

            writer.println(packet.serialize());

        } catch (Exception e) {
            Log.error("ROUTER", "Failed to forward packet: " + packet.getPacketId());
        }
    }
    
}