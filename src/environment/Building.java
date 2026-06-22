package environment;

import models.Elevator;
import models.Passenger;
import java.util.List;
import java.util.ArrayList;
import java.util.Queue;
import java.util.LinkedList;

public class Building {
    private final Floor[] floors;
    private final Object mechanicLock = new Object();
    private final List<String> completedTasks = new ArrayList<>();
    private final Queue<Elevator> brokenElevators = new LinkedList<>();

    public Building(int numFloors) {
        this.floors = new Floor[numFloors];
        for (int i = 0; i < numFloors; i++) {
            floors[i] = new Floor(i);
        }
    }

    public int getNumFloors() {
        return floors.length;
    }

    public Floor getFloor(int floorNumber) {
        return floors[floorNumber];
    }

    public boolean requestElevatorAndWait(Passenger passenger, int destinationFloor) throws InterruptedException {
        Floor current = floors[passenger.getCurrentFloor()];
        ElevatorQueue queue = current.getQueueFor(passenger);

        synchronized (passenger) {
            queue.addPassenger(passenger);
            long waitStart = System.currentTimeMillis();
            while (passenger.getCurrentFloor() != destinationFloor && passenger.isRunning()) {
                passenger.wait(3000); // Wait max 3 seconds before checking timeout
                
                // Trip cancellation (Bonus feature)
                if (passenger.getCurrentFloor() != destinationFloor && passenger.isRunning()) {
                    if (System.currentTimeMillis() - waitStart > 6000) {
                        System.out.println("[Passenger " + passenger.getId() + "] waited too long. Leaving queue and retrying.");
                        queue.removePassenger(passenger);
                        // Try again
                        queue.addPassenger(passenger);
                        waitStart = System.currentTimeMillis();
                    }
                }
            }
        }
        return passenger.getCurrentFloor() == destinationFloor;
    }

    public void passengerDroppedOff(Passenger passenger, Elevator elevator, int floorNum) {
        synchronized (passenger) {
            passenger.setCurrentFloor(floorNum);
            passenger.notifyAll();
        }
    }

    public void handleElevatorBreakdown(Elevator elevator, Passenger strandedPassenger, int floorNum) {
        synchronized (mechanicLock) {
            brokenElevators.add(elevator);
            mechanicLock.notifyAll(); // Inform mechanics
        }
        
        if (strandedPassenger != null) {
            synchronized (strandedPassenger) {
                strandedPassenger.setCurrentFloor(floorNum);
                Floor current = floors[floorNum];
                ElevatorQueue queue = current.getQueueFor(strandedPassenger);
                queue.addPassenger(strandedPassenger);
            }
        }
    }

    public Elevator findBrokenElevatorToRepair() throws InterruptedException {
        synchronized (mechanicLock) {
            while (brokenElevators.isEmpty()) {
                mechanicLock.wait(1000); 
            }
            return brokenElevators.poll();
        }
    }

    public void repairElevator(Elevator elevator) {
        synchronized (elevator) {
            elevator.notifyAll();
        }
    }

    public void waitForRepair(Elevator elevator) throws InterruptedException {
        synchronized (elevator) {
            elevator.wait(); 
        }
    }

    public void reportTaskCompleted(String taskId) {
        synchronized (completedTasks) {
            completedTasks.add(taskId);
        }
    }
    
    public List<String> getCompletedTasks() {
        return new ArrayList<>(completedTasks);
    }
}
