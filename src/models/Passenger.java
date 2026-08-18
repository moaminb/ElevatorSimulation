package models;

import environment.Building;

public abstract class Passenger implements Runnable {
    private final String id;
    private final int age;
    private final int weight;
    private final Role role;
    private final Task task;
    private int currentFloor;
    private volatile boolean taskCompleted = false;
    private volatile boolean running = true;
    private final Building building;
    private final long arrivalTime;

    public Passenger(String id, int age, int weight, Role role, Task task, int startFloor, Building building) {
        this.id = id;
        this.age = age;
        this.weight = weight;
        this.role = role;
        this.task = task;
        this.currentFloor = startFloor;
        this.building = building;
        this.arrivalTime = System.currentTimeMillis();
    }

    public String getId() { return id; }
    public int getAge() { return age; }
    public int getWeight() { return weight; }
    public int getTotalWeight() { return weight; }
    public Role getRole() { return role; }
    public Task getTask() { return task; }
    public int getCurrentFloor() { return currentFloor; }
    public void setCurrentFloor(int currentFloor) { this.currentFloor = currentFloor; }
    public boolean isRunning() { return running; }
    public boolean isTaskCompleted() { return taskCompleted; }
    public long getWaitingTime() { return System.currentTimeMillis() - arrivalTime; }
    public Building getBuilding() { return building; }

    public int getDestinationFloor() {
        if (!taskCompleted) {
            return task.getTargetFloor();
        }
        return 0;
    }

    public void stop() {
        running = false;
    }

    @Override
    public void run() {
        try {
            enterBuilding();
            travelToTaskFloor();
            if (running) {
                performTask();
            }
            if (running) {
                returnToGroundFloor();
            }
            leaveBuilding();
        } catch (InterruptedException e) {
            System.out.println("[Passenger " + id + "] interrupted.");
            Thread.currentThread().interrupt();
        }
    }

    protected void enterBuilding() {
        System.out.println("[Passenger " + id + "] (" + role + ", age: " + age + ", weight: " + getTotalWeight() + "kg) entered building at floor " + currentFloor);
    }

    protected void travelToTaskFloor() throws InterruptedException {
        int target = task.getTargetFloor();
        if (currentFloor != target) {
            boolean arrived = building.requestElevatorAndWait(this, target);
            if (arrived) {
                currentFloor = target;
                System.out.println("[Passenger " + id + "] arrived at destination floor " + currentFloor + " for task " + task.getId());
            }
        }
    }

    protected void performTask() throws InterruptedException {
        Thread.sleep(task.getDuration());
        taskCompleted = true;
        System.out.println("[Passenger " + id + "] finished task " + task.getId());
        building.reportTaskCompleted(task.getId());
    }

    protected void returnToGroundFloor() throws InterruptedException {
        if (currentFloor != 0) {
            boolean arrived = building.requestElevatorAndWait(this, 0);
            if (arrived) {
                currentFloor = 0;
            }
        }
    }

    protected void leaveBuilding() {
        System.out.println("[Passenger " + id + "] returned to ground floor and left the building.");
        building.passengerExitedBuilding(this);
    }
}
