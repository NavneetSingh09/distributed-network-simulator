package sim.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Log {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void info(String component, String message) {
        System.out.println(timestamp() + " [INFO] [" + component + "] " + message);
    }

    public static void warn(String component, String message) {
        System.out.println(timestamp() + " [WARN] [" + component + "] " + message);
    }

    public static void error(String component, String message) {
        System.out.println(timestamp() + " [ERROR] [" + component + "] " + message);
    }

    private static String timestamp() {
        return LocalDateTime.now().format(FORMATTER);
    }
}