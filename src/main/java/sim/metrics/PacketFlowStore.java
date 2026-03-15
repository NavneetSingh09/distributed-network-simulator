package sim.metrics;

import java.util.ArrayList;
import java.util.List;

public class PacketFlowStore {

    private static final List<String> flows = new ArrayList<>();

    public static synchronized void add(String flow){

        flows.add(flow);

        if(flows.size() > 100){
            flows.remove(0);
        }
    }

    public static synchronized List<String> getFlows(){
        return new ArrayList<>(flows);
    }

}