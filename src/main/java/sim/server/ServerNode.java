package sim.server;

import sim.model.Packet;
import sim.util.Log;
import sim.metrics.MetricsStore;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerNode {

    private int port;

    public ServerNode(int port) {
        this.port = port;
    }

    public void start() {

        try (ServerSocket serverSocket = new ServerSocket(port)) {

            Log.info("SERVER", "Server started on port " + port);

            while (true) {

                Socket socket = serverSocket.accept();

                BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(socket.getInputStream()));

                String data = reader.readLine();

                if (data == null) {
                    socket.close();
                    continue;
                }

                Packet packet = Packet.deserialize(data);

                Log.info("SERVER", "Packet received: " + packet.getPacketId());

                Log.info("SERVER", "Decoded message: " + packet.getFrame());

                // Update server load metrics
                if (port == 6001)
                    MetricsStore.server1Handled();
                else
                    MetricsStore.server2Handled();

                socket.close();
            }

        } catch (Exception e) {

            Log.error("SERVER", "Server failure: " + e.getMessage());

        }
    }
}