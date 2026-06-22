package models;
import environment.Building;

public class Student extends Passenger {
    public Student(String id, int age, int weight, Task task, int startFloor, Building building) {
        super(id, age, weight, Role.STUDENT, task, startFloor, building);
    }
}
