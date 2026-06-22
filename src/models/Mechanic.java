package models;

import environment.Building;

public class Mechanic extends Passenger {
    public Mechanic(String id, int age, int weight, int startFloor, Building building) {
        // Mechanics don't have a specific task in a specific floor, their task is to repair
        super(id, age, weight, Role.MECHANIC, null, startFloor, building);
    }

    @Override
    public void run() {
        try {
            System.out.println("[Mechanic " + id + "] entered building and waiting for repair calls.");
            while (running) {
                Elevator broken = building.findBrokenElevatorToRepair();
                if (broken != null) {
                    System.out.println("[Mechanic " + id + "] repairing Elevator " + broken.getId() + " at floor " + broken.getCurrentFloor());
                    Thread.sleep(2000); // Time to repair
                    building.repairElevator(broken);
                    System.out.println("[Mechanic " + id + "] finished repairing Elevator " + broken.getId());
                } else if (!running) {
                    break;
                }
            }
        } catch (InterruptedException e) {
            System.out.println("[Mechanic " + id + "] interrupted.");
            Thread.currentThread().interrupt();
        }
    }
}
