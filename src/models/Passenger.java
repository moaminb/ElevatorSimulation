package models;

import environment.Building;

public abstract class Passenger implements Runnable {
    protected final String id;
    protected final int age;
    protected final int weight;
    protected final Role role;
    protected final Task task;
    protected int currentFloor;
    protected volatile boolean running = true;
    protected final Building building;

    public Passenger(String id, int age, int weight, Role role, Task task, int startFloor, Building building) {
        this.id = id;
        this.age = age;
        this.weight = weight;
        this.role = role;
        this.task = task;
        this.currentFloor = startFloor;
        this.building = building;
    }

    public String getId() { return id; }
    public int getAge() { return age; }
    public int getWeight() { return weight; }
    public int getTotalWeight() { return weight; } // Overridden by Porter
    public Role getRole() { return role; }
    public Task getTask() { return task; }
    public int getCurrentFloor() { return currentFloor; }
    public void setCurrentFloor(int currentFloor) { this.currentFloor = currentFloor; }
    public boolean isRunning() { return running; }

    public void stop() {
        running = false;
    }

    @Override
    public void run() {
        try {
            System.out.println("[Passenger " + id + "] (" + role + ") entered building at floor " + currentFloor);
            
            // 1. Wait for elevator and travel to task floor
            boolean arrived = building.requestElevatorAndWait(this, task.getTargetFloor());
            
            if (arrived && running) {
                currentFloor = task.getTargetFloor();
                System.out.println("[Passenger " + id + "] arrived at destination floor " + currentFloor + " for task " + task.getId());
                
                // Do task
                Thread.sleep(task.getDuration());
                System.out.println("[Passenger " + id + "] finished task " + task.getId());
                building.reportTaskCompleted(task.getId());
                
                // 2. Return to ground floor
                if (currentFloor != 0) {
                    arrived = building.requestElevatorAndWait(this, 0);
                    if (arrived && running) {
                        currentFloor = 0;
                        System.out.println("[Passenger " + id + "] returned to ground floor and left the building.");
                    }
                } else {
                    System.out.println("[Passenger " + id + "] is already at ground floor, leaving the building.");
                }
            }
        } catch (InterruptedException e) {
            System.out.println("[Passenger " + id + "] was interrupted.");
            Thread.currentThread().interrupt();
        }
    }
}
