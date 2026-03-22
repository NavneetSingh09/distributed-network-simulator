package sim.model;

import java.util.UUID;

public class Packet {
    private String packetId;
    private String sourceIp;
    private String destinationIp;
    private String kind;   
    private String frame;  

    public Packet() {
    }

    public Packet(String sourceIp, String destinationIp, String kind, String frame) {
        this.packetId = UUID.randomUUID().toString();
        this.sourceIp = sourceIp;
        this.destinationIp = destinationIp;
        this.kind = kind;
        this.frame = frame;
    }

    public String getPacketId() {
        return packetId;
    }

    public void setPacketId(String packetId) {
        this.packetId = packetId;
    }

    public String getSourceIp() {
        return sourceIp;
    }

    public void setSourceIp(String sourceIp) {
        this.sourceIp = sourceIp;
    }

    public String getDestinationIp() {
        return destinationIp;
    }

    public void setDestinationIp(String destinationIp) {
        this.destinationIp = destinationIp;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getFrame() {
        return frame;
    }

    public void setFrame(String frame) {
        this.frame = frame;
    }

    public static Packet newRequest(String sourceIp, String destinationIp, String frame) {
        return new Packet(sourceIp, destinationIp, "REQUEST", frame);
    }

    public static Packet newResponse(Packet request, String frame) {
        Packet response = new Packet(request.destinationIp, request.sourceIp, "RESPONSE", frame);
        response.setPacketId(request.getPacketId());
        return response;
    }

    public String serialize() {
        return escape(packetId) + ";" +
               escape(sourceIp) + ";" +
               escape(destinationIp) + ";" +
               escape(kind) + ";" +
               escape(frame);
    }

    public static Packet deserialize(String data) {
        String[] parts = data.split(";", 5);

        Packet packet = new Packet();
        packet.setPacketId(unescape(parts[0]));
        packet.setSourceIp(unescape(parts[1]));
        packet.setDestinationIp(unescape(parts[2]));
        packet.setKind(unescape(parts[3]));
        packet.setFrame(unescape(parts[4]));

        return packet;
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace(";", "\\;");
    }

    private static String unescape(String value) {
        StringBuilder sb = new StringBuilder();
        boolean escaped = false;

        for (char ch : value.toCharArray()) {
            if (escaped) {
                sb.append(ch);
                escaped = false;
            } else if (ch == '\\') {
                escaped = true;
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return "Packet{" +
                "packetId='" + packetId + '\'' +
                ", sourceIp='" + sourceIp + '\'' +
                ", destinationIp='" + destinationIp + '\'' +
                ", kind='" + kind + '\'' +
                ", frame='" + frame + '\'' +
                '}';
    }
}