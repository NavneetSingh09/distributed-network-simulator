package sim.server;

import sim.config.Ports;
import sim.metrics.MetricsStore;
import sim.model.Packet;
import sim.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;


public class ServerNode {

    private final int          port;
    private final MetricsStore metricsStore;

    public ServerNode(int port, MetricsStore metricsStore) {
        this.port         = port;
        this.metricsStore = metricsStore;
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {

            Log.info("SERVER", "Server started on port " + port);

            while (true) {
                Socket socket = serverSocket.accept();
                try (socket;
                     BufferedReader reader = new BufferedReader(
                             new InputStreamReader(socket.getInputStream()))) {

                    String data = reader.readLine();
                    if (data == null) continue;

                    Packet packet = Packet.deserialize(data);
                    Log.info("SERVER", "Packet received: " + packet.getPacketId());
                    Log.info("SERVER", "Decoded message: " + packet.getFrame());

                    if (port == Ports.SERVER1_PORT)
                        metricsStore.server1Handled();
                    else
                        metricsStore.server2Handled();
                }
            }

        } catch (Exception e) {
            Log.error("SERVER", "Server failure on port " + port + ": " + e.getMessage());
        }
    }
}
