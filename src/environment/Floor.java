package environment;

import models.Elevator;
import models.Passenger;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;

public class Floor {
    private final int floorNumber;
    private final Map<Elevator, ElevatorQueue> elevatorQueues = new LinkedHashMap<>();

    public Floor(int floorNumber) {
        this.floorNumber = floorNumber;
    }

    public int getFloorNumber() {
        return floorNumber;
    }

    public synchronized void addElevatorQueue(Elevator elevator) {
        elevatorQueues.put(elevator, new ElevatorQueue());
    }

    public synchronized ElevatorQueue getQueueFor(Elevator elevator) {
        return elevatorQueues.get(elevator);
    }

    public synchronized List<ElevatorQueue> getEligibleQueues(Passenger passenger) {
        List<ElevatorQueue> eligible = new ArrayList<>();
        for (Map.Entry<Elevator, ElevatorQueue> entry : elevatorQueues.entrySet()) {
            if (entry.getKey().canAccept(passenger)) {
                eligible.add(entry.getValue());
            }
        }
        return eligible;
    }

    /**
     * انتخاب مناسب‌ترین صف (با کمترین طول انتظار) برای مسافر
     */
    public synchronized ElevatorQueue getPreferredQueue(Passenger passenger) {
        List<ElevatorQueue> eligible = getEligibleQueues(passenger);
        if (eligible.isEmpty()) {
            return null;
        }

        ElevatorQueue bestQueue = eligible.get(0);
        for (ElevatorQueue q : eligible) {
            if (q.size() < bestQueue.size()) {
                bestQueue = q;
            }
        }
        return bestQueue;
    }
}
