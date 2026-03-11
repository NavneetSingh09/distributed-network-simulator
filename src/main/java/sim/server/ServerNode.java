package sim.server;

import sim.model.Packet;
import sim.osi.OsiStack;
import sim.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerNode {

    private int port;
    private OsiStack osiStack;

    public ServerNode(int port) {
        this.port = port;
        this.osiStack = new OsiStack();
    }

    public void start() {

        try (ServerSocket serverSocket = new ServerSocket(port)) {

            Log.info("SERVER", "Server started on port " + port);

            while (true) {

                Socket clientSocket = serverSocket.accept();

                BufferedReader reader =
                        new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                PrintWriter writer =
                        new PrintWriter(clientSocket.getOutputStream(), true);

                String data = reader.readLine();

                Packet packet = Packet.deserialize(data);

                Log.info("SERVER", "Packet received: " + packet.getPacketId());

                String message = osiStack.decapsulate(packet.getFrame());

                Log.info("SERVER", "Decoded message: " + message);

                // Create response
                Packet response =
                        Packet.newResponse(packet, osiStack.encapsulate("ACK: Message received"));

                writer.println(response.serialize());

                clientSocket.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}