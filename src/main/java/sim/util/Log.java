package sim.util;

import sim.metrics.LogStore;

import java.time.LocalDateTime;

public class Log {

    public static void info(String component, String message) {

        String log = LocalDateTime.now() +
                " [INFO] [" + component + "] " + message;

        System.out.println(log);

        LogStore.add(log);
    }

    public static void warn(String component, String message) {

        String log = LocalDateTime.now() +
                " [WARN] [" + component + "] " + message;

        System.out.println(log);

        LogStore.add(log);
    }

    public static void error(String component, String message) {

        String log = LocalDateTime.now() +
                " [ERROR] [" + component + "] " + message;

        System.out.println(log);

        LogStore.add(log);
    }
}