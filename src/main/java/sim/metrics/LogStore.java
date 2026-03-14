package sim.metrics;

import java.util.ArrayList;
import java.util.List;

public class LogStore {

    private static final List<String> logs = new ArrayList<>();

    public static synchronized void add(String message) {

        if (logs.size() > 200) {
            logs.remove(0);
        }

        logs.add(message);
    }

    public static List<String> getLogs() {
        return logs;
    }
}