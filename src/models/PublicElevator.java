package models;
import environment.Building;

public class PublicElevator extends Elevator {
    public PublicElevator(String id, int maxWeight, Building building) {
        super(id, maxWeight, building);
    }

    @Override
    public boolean canAccept(Passenger passenger) {
        if (passenger.getRole() == Role.PORTER) return false;
        return passenger.getTotalWeight() <= getMaxWeight();
    }
}
