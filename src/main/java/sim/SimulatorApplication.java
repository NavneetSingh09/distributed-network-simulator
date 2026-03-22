package sim;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import sim.config.Ports;
import sim.config.ServerStatusConfig;
import sim.metrics.MetricsStore;
import sim.router.Router;
import sim.server.ServerNode;
import sim.service.SimulatorService;

@SpringBootApplication
@EnableScheduling
public class SimulatorApplication {

    public static void main(String[] args) {
        ApplicationContext ctx = SpringApplication.run(SimulatorApplication.class, args);

        String role = System.getenv("NODE_ROLE");
        if (role == null) role = "api";

        switch (role) {
            case "router" -> {
                System.out.println("Starting as ROUTER");
                Router router = ctx.getBean(Router.class);
                new Thread(router::start, "router-thread").start();
                ctx.getBean(SimulatorService.class).startTraffic();
            }
            case "server1" -> {
                System.out.println("Starting as SERVER1");
                MetricsStore metrics = ctx.getBean(MetricsStore.class);
                ctx.getBean(ServerStatusConfig.class).setServer1Up(true);
                new Thread(() -> new ServerNode(Ports.SERVER1_PORT, metrics).start(),
                        "server1-thread").start();
            }
            case "server2" -> {
                System.out.println("Starting as SERVER2");
                MetricsStore metrics = ctx.getBean(MetricsStore.class);
                ctx.getBean(ServerStatusConfig.class).setServer2Up(true);
                new Thread(() -> new ServerNode(Ports.SERVER2_PORT, metrics).start(),
                        "server2-thread").start();
            }
            default ->
                System.out.println("Starting as API only (no node auto-started)");
        }
    }
}