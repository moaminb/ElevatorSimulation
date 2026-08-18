package models;
import environment.Building;

public class FreightElevator extends Elevator {
    public FreightElevator(String id, int maxWeight, Building building) {
        super(id, maxWeight, building);
    }

    @Override
    public boolean canAccept(Passenger passenger) {
        if (passenger.getRole() != Role.PORTER) return false;
        return passenger.getTotalWeight() <= getMaxWeight();
    }
}
