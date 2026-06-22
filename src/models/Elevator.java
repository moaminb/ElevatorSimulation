package models;

import environment.Building;
import environment.Floor;
import environment.ElevatorQueue;
import java.util.Random;

public abstract class Elevator implements Runnable {
    protected final String id;
    protected int currentFloor = 0;
    protected Passenger currentPassenger = null;
    protected final int maxWeight;
    protected volatile boolean running = true;
    protected final long travelTimeMs = 300; // Fast simulation
    protected final Building building;
    protected final Random random = new Random();

    public Elevator(String id, int maxWeight, Building building) {
        this.id = id;
        this.maxWeight = maxWeight;
        this.building = building;
    }

    public String getId() { return id; }
    public int getCurrentFloor() { return currentFloor; }
    public boolean isRunning() { return running; }
    
    public abstract boolean canAccept(Passenger passenger);

    public void stop() {
        running = false;
    }

    @Override
    public void run() {
        while (running) {
            try {
                if (currentPassenger != null) {
                    // Determine destination
                    int targetFloor = 0;
                    if (currentPassenger.getCurrentFloor() == 0) {
                        targetFloor = currentPassenger.getTask().getTargetFloor();
                    }
                    
                    moveToFloor(targetFloor);
                    if (!running) break;
                    
                    if (random.nextDouble() < 0.05) { // 5% chance of breakdown
                        System.out.println("[Elevator " + id + "] BROKE DOWN at floor " + currentFloor + "!");
                        Passenger stranded = currentPassenger;
                        currentPassenger = null;
                        
                        building.handleElevatorBreakdown(this, stranded, currentFloor);
                        building.waitForRepair(this);
                        System.out.println("[Elevator " + id + "] REPAIRED at floor " + currentFloor);
                    } else {
                        System.out.println("[Elevator " + id + "] dropped off Passenger " + currentPassenger.getId() + " at floor " + currentFloor);
                        building.passengerDroppedOff(currentPassenger, this, currentFloor);
                        currentPassenger = null;
                    }
                } else {
                    Floor floor = building.getFloor(currentFloor);
                    ElevatorQueue queue = floor.getQueueFor(this);
                    
                    Passenger next = queue.pickupPassenger(this);
                    if (next != null) {
                        currentPassenger = next;
                        System.out.println("[Elevator " + id + "] picked up Passenger " + currentPassenger.getId() + " at floor " + currentFloor);
                    } else {
                        // Sweep to the next floor to find passengers
                        currentFloor = (currentFloor + 1) % building.getNumFloors();
                        Thread.sleep(travelTimeMs);
                    }
                }
            } catch (InterruptedException e) {
                System.out.println("[Elevator " + id + "] interrupted.");
                Thread.currentThread().interrupt();
            }
        }
    }
    
    protected void moveToFloor(int targetFloor) throws InterruptedException {
        while (currentFloor != targetFloor && running) {
            Thread.sleep(travelTimeMs);
            if (currentFloor < targetFloor) currentFloor++;
            else currentFloor--;
            // System.out.println("[Elevator " + id + "] passing floor " + currentFloor);
        }
    }
}
