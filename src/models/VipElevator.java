package models;
import environment.Building;

public class VipElevator extends Elevator {
    public VipElevator(String id, int maxWeight, Building building) {
        super(id, maxWeight, building);
    }

    @Override
    public boolean canAccept(Passenger passenger) {
        // VIP accepts Professors and Deputies
        if (passenger.getRole() != Role.PROFESSOR && passenger.getRole() != Role.DEPUTY) return false;
        return passenger.getTotalWeight() <= getMaxWeight();
    }
}
