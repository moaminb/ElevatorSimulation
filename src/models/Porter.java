package models;
import environment.Building;

public class Porter extends Passenger {
    private final int cargoWeight;

    public Porter(String id, int age, int weight, int cargoWeight, Task task, int startFloor, Building building) {
        super(id, age, weight, Role.PORTER, task, startFloor, building);
        this.cargoWeight = cargoWeight;
    }

    public int getCargoWeight() {
        return cargoWeight;
    }

    @Override
    public int getTotalWeight() {
        return super.getTotalWeight() + cargoWeight;
    }
}
