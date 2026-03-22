package sim.server;

import sim.config.Ports;
import sim.metrics.MetricsStore;
import sim.model.Packet;
import sim.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;

/**
 * Receives packets forwarded by the Router.
 * Reports back to the router via HTTP so metrics are centralized.
 */
public class ServerNode {

    private final int          port;
    private final MetricsStore metricsStore;
    private final int          serverId;
    private final String       routerHost;

    public ServerNode(int port, MetricsStore metricsStore) {
        this.port         = port;
        this.metricsStore = metricsStore;
        this.serverId     = (port == Ports.SERVER1_PORT) ? 1 : 2;
        this.routerHost   = System.getenv("ROUTER_HOST") != null
                            ? System.getenv("ROUTER_HOST") : "localhost";
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {

            Log.info("SERVER", "Server " + serverId + " started on port " + port);

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

                    // Update local metrics
                    if (port == Ports.SERVER1_PORT)
                        metricsStore.server1Handled();
                    else
                        metricsStore.server2Handled();

                    // Report back to router so its MetricsStore stays in sync
                    reportToRouter();
                }
            }

        } catch (Exception e) {
            Log.error("SERVER", "Server failure on port " + port + ": " + e.getMessage());
        }
    }

    private void reportToRouter() {
        try {
            URL url = new URL("http://" + routerHost + ":8080/api/metrics/server-handled?serverId=" + serverId);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(1000);
            conn.setReadTimeout(1000);
            conn.getResponseCode(); // fire and read response
            conn.disconnect();
        } catch (Exception e) {
            Log.error("SERVER", "Failed to report to router: " + e.getMessage());
        }
    }
}