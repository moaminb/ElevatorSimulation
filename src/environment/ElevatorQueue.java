package environment;

import java.util.ArrayList;
import java.util.List;
import models.Passenger;
import models.Elevator;

public class ElevatorQueue {
    private final List<Passenger> waitingPassengers = new ArrayList<>();

    public synchronized void addPassenger(Passenger passenger) {
        waitingPassengers.add(passenger);
        notifyAll(); 
    }

    public synchronized void removePassenger(Passenger passenger) {
        waitingPassengers.remove(passenger);
    }

    public synchronized Passenger pickupPassenger(Elevator elevator) throws InterruptedException {
        if (waitingPassengers.isEmpty()) {
            return null;
        }

        Passenger selected = selectFairPassenger(elevator);
        if (selected != null) {
            waitingPassengers.remove(selected);
        }
        return selected;
    }

    private Passenger selectFairPassenger(Elevator elevator) {
        Passenger best = null;
        for (Passenger p : waitingPassengers) {
            if (elevator.canAccept(p)) {
                if (best == null) {
                    best = p;
                } else {
                    if (p.getTask().getPriority().getLevel() > best.getTask().getPriority().getLevel()) {
                        best = p;
                    } else if (p.getTask().getPriority().getLevel() == best.getTask().getPriority().getLevel()) {
                        if (p.getAge() > best.getAge()) {
                            best = p;
                        }
                    }
                }
            }
        }
        return best;
    }
}
