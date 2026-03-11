package sim.osi;

public class OsiStack {

    // Encapsulation: APP → TCP → IP → MAC → PHY
    public String encapsulate(String payload) {

        String application = "[APP]" + payload;

        String transport = "[TCP]" + application;

        String network = "[IP]" + transport;

        String dataLink = "[MAC]" + network;

        String physical = "[PHY]" + dataLink;

        return physical;
    }

    // Reverse process (Server side)
    public String decapsulate(String frame) {

        frame = frame.replace("[PHY]", "");
        frame = frame.replace("[MAC]", "");
        frame = frame.replace("[IP]", "");
        frame = frame.replace("[TCP]", "");
        frame = frame.replace("[APP]", "");

        return frame;
    }
}