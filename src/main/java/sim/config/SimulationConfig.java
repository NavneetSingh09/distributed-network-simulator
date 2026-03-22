package sim.config;

import org.springframework.stereotype.Component;


@Component
public class SimulationConfig {

    private volatile double dropRate   = 0.10;
    private volatile int    minLatency = 50;
    private volatile int    maxLatency = 300;

    public double getDropRate()   { return dropRate; }
    public int    getMinLatency() { return minLatency; }
    public int    getMaxLatency() { return maxLatency; }

    public void setDropRate(double dropRate) {
        if (dropRate < 0 || dropRate > 1)
            throw new IllegalArgumentException("dropRate must be between 0 and 1");
        this.dropRate = dropRate;
    }

    public void setMinLatency(int minLatency) {
        if (minLatency < 0)
            throw new IllegalArgumentException("minLatency must be >= 0");
        this.minLatency = minLatency;
    }

    public void setMaxLatency(int maxLatency) {
        if (maxLatency <= minLatency)
            throw new IllegalArgumentException("maxLatency must be > minLatency");
        this.maxLatency = maxLatency;
    }
}
