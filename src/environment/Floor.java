package environment;

import models.Elevator;
import models.Passenger;
import models.Role;
import models.PublicElevator;
import models.VipElevator;
import models.FreightElevator;

public class Floor {
    private final int floorNumber;
    private final ElevatorQueue publicQueue = new ElevatorQueue();
    private final ElevatorQueue vipQueue = new ElevatorQueue();
    private final ElevatorQueue freightQueue = new ElevatorQueue();

    public Floor(int floorNumber) {
        this.floorNumber = floorNumber;
    }

    public ElevatorQueue getQueueFor(Elevator elevator) {
        if (elevator instanceof PublicElevator) return publicQueue;
        if (elevator instanceof VipElevator) return vipQueue;
        return freightQueue;
    }

    public ElevatorQueue getQueueFor(Passenger passenger) {
        if (passenger.getRole() == Role.PORTER) return freightQueue;
        if (passenger.getRole() == Role.PROFESSOR || passenger.getRole() == Role.DEPUTY) return vipQueue;
        return publicQueue;
    }
}
