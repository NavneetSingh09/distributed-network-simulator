package sim.config;

public class Ports {
    public static final int ROUTER_PORT  = 5000;
    public static final int SERVER1_PORT = 6001;
    public static final int SERVER2_PORT = 6002;

    public static String getServer1Host() {
        String h = System.getenv("SERVER1_HOST");
        return h != null ? h : "localhost";
    }

    public static String getServer2Host() {
        String h = System.getenv("SERVER2_HOST");
        return h != null ? h : "localhost";
    }

    private Ports() {}
}