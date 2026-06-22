package models;
import environment.Building;

public class Professor extends Passenger {
    public Professor(String id, int age, int weight, Task task, int startFloor, Building building) {
        super(id, age, weight, Role.PROFESSOR, task, startFloor, building);
    }
}
