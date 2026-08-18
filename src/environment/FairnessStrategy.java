package environment;

import java.util.List;
import models.Elevator;
import models.Passenger;

@FunctionalInterface
public interface FairnessStrategy {
    Passenger selectPassenger(List<Passenger> waiting, Elevator elevator);
}
