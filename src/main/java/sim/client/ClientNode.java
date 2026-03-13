package sim.client;

import sim.config.Ports;
import sim.model.Packet;
import sim.osi.OsiStack;
import sim.util.Log;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ClientNode {

    private String clientIp;
    private String serverIp;
    private OsiStack osiStack;

    public ClientNode(String clientIp, String serverIp) {
        this.clientIp = clientIp;
        this.serverIp = serverIp;
        this.osiStack = new OsiStack();
    }

    public void start() {

        Scanner scanner = new Scanner(System.in);

        Log.info("CLIENT", "Client started");

        while (true) {

            System.out.print("Enter message: ");
            String message = scanner.nextLine();

            try {

                String frame = osiStack.encapsulate(message);

                Packet packet = Packet.newRequest(clientIp, serverIp, frame);

                Socket routerSocket = new Socket("localhost", Ports.ROUTER_PORT);

                PrintWriter writer =
                        new PrintWriter(routerSocket.getOutputStream(), true);

                writer.println(packet.serialize());

                Log.info("CLIENT", "Packet sent: " + packet.getPacketId());

                routerSocket.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}