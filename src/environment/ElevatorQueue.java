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

    public synchronized int size() {
        return waitingPassengers.size();
    }

    /**
     * انتخاب مسافر با استفاده از الگوی Strategy و بدون نیاز به switch-case (رعایت کامل OCP و DIP)
     */
    public synchronized Passenger pickupPassenger(Elevator elevator, FairnessStrategy strategy) {
        if (waitingPassengers.isEmpty() || strategy == null) {
            return null;
        }

        Passenger selected = strategy.selectPassenger(waitingPassengers, elevator);
        if (selected != null) {
            waitingPassengers.remove(selected);
        }
        return selected;
    }
}
