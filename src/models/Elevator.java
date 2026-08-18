package models;

import environment.Building;
import environment.Floor;
import environment.ElevatorQueue;

public abstract class Elevator implements Runnable {
    private final String id;
    private int currentFloor = 0;
    private Passenger currentPassenger = null;
    private final int maxWeight;
    private volatile boolean running = true;
    private final long travelTimeMs = 150;
    private final Building building;

    public Elevator(String id, int maxWeight, Building building) {
        this.id = id;
        this.maxWeight = maxWeight;
        this.building = building;
    }

    public String getId() { return id; }
    public int getCurrentFloor() { return currentFloor; }
    public boolean isRunning() { return running; }
    public int getMaxWeight() { return maxWeight; }
    public Building getBuilding() { return building; }
    public Passenger getCurrentPassenger() { return currentPassenger; }

    public abstract boolean canAccept(Passenger passenger);

    public void stop() {
        running = false;
    }

    @Override
    public void run() {
        while (running) {
            try {
                if (currentPassenger != null) {
                    serveCurrentPassenger();
                } else {
                    lookForPassengerAtCurrentFloor();
                }
            } catch (InterruptedException e) {
                System.out.println("[Elevator " + id + "] stopped.");
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    protected void serveCurrentPassenger() throws InterruptedException {
        int targetFloor = currentPassenger.getDestinationFloor();
        moveToFloor(targetFloor);
        if (running && currentPassenger != null) {
            deliverPassenger();
        }
    }

    protected void deliverPassenger() {
        System.out.println("[Elevator " + id + "] dropped off Passenger " + currentPassenger.getId() + " at floor " + currentFloor);
        building.releaseSystemWeight(currentPassenger.getTotalWeight());
        building.passengerDroppedOff(currentPassenger, this, currentFloor);
        currentPassenger = null;
    }

    protected void lookForPassengerAtCurrentFloor() throws InterruptedException {
        announceArrivalAtFloor(currentFloor);

        Floor floor = building.getFloor(currentFloor);
        ElevatorQueue queue = floor.getQueueFor(this);
        
        if (queue != null) {
            Passenger next = queue.pickupPassenger(this, building.getFairnessMode());
            if (next != null) {
                building.acquireSystemWeight(next.getTotalWeight());
                currentPassenger = next;
                System.out.println("[Elevator " + id + "] picked up Passenger " + currentPassenger.getId() + 
                    " at floor " + currentFloor + " (System load: " + building.getCurrentSystemWeight() + "/" + building.getMaxSystemWeight() + "kg)");
                return;
            }
        }

        int targetFloor = building.findNearestFloorWithWaitingPassenger(this, currentFloor);
        if (targetFloor != -1) {
            moveToFloor(targetFloor);
        } else {
            Thread.sleep(travelTimeMs);
        }
    }

    protected void announceArrivalAtFloor(int floorNum) {
        Floor floor = building.getFloor(floorNum);
        ElevatorQueue queue = floor.getQueueFor(this);
        if (queue != null && queue.size() > 0) {
            System.out.println("[Elevator " + id + "] arrived at floor " + floorNum + " and ready for service.");
        }
    }

    protected void moveToFloor(int targetFloor) throws InterruptedException {
        while (currentFloor != targetFloor && running) {
            Thread.sleep(travelTimeMs);
            if (currentFloor < targetFloor) {
                currentFloor++;
            } else {
                currentFloor--;
            }
        }
    }
}
