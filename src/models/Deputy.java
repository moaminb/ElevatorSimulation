package models;
import environment.Building;

public class Deputy extends Passenger {
    public Deputy(String id, int age, int weight, Task task, int startFloor, Building building) {
        super(id, age, weight, Role.DEPUTY, task, startFloor, building);
    }
}
