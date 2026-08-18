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

    /**
     * وظیفه ۱: انتقال مسافر سوارشده به مقصد مشخص‌شده و پیاده کردن وی
     */
    protected void serveCurrentPassenger() throws InterruptedException {
        int targetFloor = currentPassenger.getDestinationFloor();
        moveToFloor(targetFloor);
        if (running && currentPassenger != null) {
            deliverPassenger();
        }
    }

    /**
     * وظیفه ۲: پیاده‌سازی، آزادسازی وزن سیستم و اعلام رسیدن به مسافر
     */
    protected void deliverPassenger() {
        System.out.println("[Elevator " + id + "] dropped off Passenger " + currentPassenger.getId() + " at floor " + currentFloor);
        // بخش امتیازی: آزادسازی وزن مسافر در کل سیستم
        building.releaseSystemWeight(currentPassenger.getTotalWeight());
        building.passengerDroppedOff(currentPassenger, this, currentFloor);
        currentPassenger = null;
    }

    /**
     * وظیفه ۳: بررسی صف اختصاصی این آسانسور در طبقه فعلی و سوار کردن مسافر
     */
    protected void lookForPassengerAtCurrentFloor() throws InterruptedException {
        // اعلام حضور در طبقه فعلی
        announceArrivalAtFloor(currentFloor);

        Floor floor = building.getFloor(currentFloor);
        ElevatorQueue queue = floor.getQueueFor(this);
        
        if (queue != null) {
            Passenger next = queue.pickupPassenger(this, building.getFairnessMode());
            if (next != null) {
                // بخش امتیازی: بررسی و رزرو وزن در سقف کل سیستم
                building.acquireSystemWeight(next.getTotalWeight());
                currentPassenger = next;
                System.out.println("[Elevator " + id + "] picked up Passenger " + currentPassenger.getId() + 
                    " at floor " + currentFloor + " (System load: " + building.getCurrentSystemWeight() + "/" + building.getMaxSystemWeight() + "kg)");
                return;
            }
        }

        // حرکت هوشمند به سمت نزدیک‌ترین طبقه‌ای که مسافر منتظر دارد
        int targetFloor = building.findNearestFloorWithWaitingPassenger(this, currentFloor);
        if (targetFloor != -1) {
            moveToFloor(targetFloor);
        } else {
            // در صورتی که در کل ساختمان مسافری منتظر نباشد، استراحت کوتاه
            Thread.sleep(travelTimeMs);
        }
    }

    /**
     * وظیفه ۴: اعلام حضور آسانسور در طبقه به مسافران منتظر (مطابق با صورت تمرین)
     */
    protected void announceArrivalAtFloor(int floorNum) {
        Floor floor = building.getFloor(floorNum);
        ElevatorQueue queue = floor.getQueueFor(this);
        if (queue != null && queue.size() > 0) {
            System.out.println("[Elevator " + id + "] arrived at floor " + floorNum + " and ready for service.");
        }
    }

    /**
     * وظیفه ۵: شبیه‌سازی حرکت فیزیکی طبقه به طبقه آسانسور
     */
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
