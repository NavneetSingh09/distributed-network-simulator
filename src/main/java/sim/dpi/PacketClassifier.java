package sim.dpi;

import org.springframework.stereotype.Component;

/**
 * Deep Packet Inspection — classifies packets based on payload content.
 * In a real DPI system this would inspect actual protocol headers.
 * Here we simulate it by inspecting the payload string.
 */
@Component
public class PacketClassifier {

    public enum Protocol {
        HTTP, DNS, FTP, ICMP, SUSPICIOUS, UNKNOWN
    }

    public Protocol classify(String payload) {
        if (payload == null || payload.isEmpty()) return Protocol.UNKNOWN;

        String p = payload.toUpperCase();

        // Suspicious traffic check first — highest priority
        if (containsSuspicious(p)) return Protocol.SUSPICIOUS;

        if (p.contains("HTTP") || p.contains("GET") ||
            p.contains("POST") || p.contains("HTML"))
            return Protocol.HTTP;

        if (p.contains("DNS") || p.contains("RESOLVE") ||
            p.contains("QUERY"))
            return Protocol.DNS;

        if (p.contains("FTP") || p.contains("UPLOAD") ||
            p.contains("DOWNLOAD"))
            return Protocol.FTP;

        if (p.contains("PING") || p.contains("ICMP") ||
            p.contains("ECHO"))
            return Protocol.ICMP;

        return Protocol.UNKNOWN;
    }

    private boolean containsSuspicious(String payload) {
        return payload.contains("ATTACK")   ||
               payload.contains("MALWARE")  ||
               payload.contains("FLOOD")    ||
               payload.contains("EXPLOIT")  ||
               payload.contains("INJECT")   ||
               payload.contains("DROP TABLE");
    }
}