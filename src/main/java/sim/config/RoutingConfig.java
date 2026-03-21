package sim.config;

import org.springframework.stereotype.Component;


@Component
public class RoutingConfig {

    public enum Algorithm {
        ROUND_ROBIN,
        LEAST_LOAD
    }

    private volatile Algorithm algorithm = Algorithm.ROUND_ROBIN;

    public Algorithm getAlgorithm() { return algorithm; }

    public void setAlgorithm(String name) {
        this.algorithm = Algorithm.valueOf(name.toUpperCase());
    }
}
