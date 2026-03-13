package sim;

import sim.client.ClientNode;
import sim.client.TrafficSimulator;
import sim.router.Router;
import sim.server.ServerNode;
import sim.config.Ports;

public class Main {

    public static void main(String[] args) {

        if (args.length == 0) {
            System.out.println("Usage: router | server1 | server2 | client | traffic");
            return;
        }

        switch (args[0]) {

            case "router":
                new Router().start();
                break;

            case "server1":
                new ServerNode(Ports.SERVER1_PORT).start();
                break;

            case "server2":
                new ServerNode(Ports.SERVER2_PORT).start();
                break;

            case "client":
                new ClientNode("10.0.0.1", "10.0.0.5").start();
                break;

            case "traffic":
                new TrafficSimulator().start();
                break;

            default:
                System.out.println("Invalid argument");
                System.out.println("Usage: router | server1 | server2 | client | traffic");
        }
    }
}