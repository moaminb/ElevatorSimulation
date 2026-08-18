package environment;

import models.Elevator;
import models.Passenger;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

public class Building {
    private final Floor[] floors;
    private final List<Elevator> elevators = new ArrayList<>();
    private final List<String> completedTasks = Collections.synchronizedList(new ArrayList<>());
    private final AtomicInteger exitedPassengersCount = new AtomicInteger(0);
    private final FairnessMode fairnessMode;

    // بخش امتیازی: محدودیت ثابت مجموع وزن مسافران کل آسانسورها در سیستم
    private final int maxSystemWeight;
    private int currentSystemWeight = 0;

    public Building(int numFloors, FairnessMode fairnessMode) {
        this(numFloors, fairnessMode, 2500); // پیش‌فرض: حداکثر ۲۵۰۰ کیلوگرم وزن کل سیستم
    }

    public Building(int numFloors, FairnessMode fairnessMode, int maxSystemWeight) {
        this.fairnessMode = fairnessMode;
        this.maxSystemWeight = maxSystemWeight;
        this.floors = new Floor[numFloors];
        for (int i = 0; i < numFloors; i++) {
            floors[i] = new Floor(i);
        }
    }

    public synchronized void addElevator(Elevator elevator) {
        elevators.add(elevator);
        for (Floor floor : floors) {
            floor.addElevatorQueue(elevator);
        }
    }

    public int getNumFloors() {
        return floors.length;
    }

    public Floor getFloor(int floorNumber) {
        return floors[floorNumber];
    }

    public FairnessMode getFairnessMode() {
        return fairnessMode;
    }

    public int getMaxSystemWeight() {
        return maxSystemWeight;
    }

    public synchronized int getCurrentSystemWeight() {
        return currentSystemWeight;
    }

    /**
     * بخش امتیازی: دریافت سهمیه وزنی از سیستم قبل از سوار کردن مسافر
     */
    public synchronized void acquireSystemWeight(int weight) throws InterruptedException {
        while (currentSystemWeight + weight > maxSystemWeight) {
            wait();
        }
        currentSystemWeight += weight;
    }

    /**
     * بخش امتیازی: آزادسازی سهمیه وزنی پس از پیاده کردن مسافر
     */
    public synchronized void releaseSystemWeight(int weight) {
        currentSystemWeight -= weight;
        notifyAll();
    }

    /**
     * یافتن نزدیک‌ترین طبقه دارای مسافر منتظر برای یک آسانسور خاص
     */
    public int findNearestFloorWithWaitingPassenger(Elevator elevator, int currentFloor) {
        int bestFloor = -1;
        int minDistance = Integer.MAX_VALUE;

        for (int f = 0; f < floors.length; f++) {
            ElevatorQueue queue = floors[f].getQueueFor(elevator);
            if (queue != null && queue.size() > 0) {
                int distance = Math.abs(f - currentFloor);
                if (distance < minDistance) {
                    minDistance = distance;
                    bestFloor = f;
                }
            }
        }
        return bestFloor;
    }

    /**
     * درخواست آسانسور توسط مسافر و قرارگیری در صف تا زمان رسیدن به مقصد
     */
    public boolean requestElevatorAndWait(Passenger passenger, int destinationFloor) throws InterruptedException {
        Floor currentFloor = floors[passenger.getCurrentFloor()];
        ElevatorQueue queue = currentFloor.getPreferredQueue(passenger);
        if (queue == null) {
            System.err.println("[Error] No eligible elevator queue found for passenger " + passenger.getId());
            return false;
        }

        synchronized (passenger) {
            queue.addPassenger(passenger);
            while (passenger.getCurrentFloor() != destinationFloor && passenger.isRunning()) {
                passenger.wait();
            }
        }
        return passenger.getCurrentFloor() == destinationFloor;
    }

    /**
     * پیاده کردن مسافر در طبقه و آگاه‌سازی نخ مسافر
     */
    public void passengerDroppedOff(Passenger passenger, Elevator elevator, int floorNum) {
        synchronized (passenger) {
            passenger.setCurrentFloor(floorNum);
            passenger.notifyAll();
        }
    }

    /**
     * ثبت تسک تکمیل‌شده به صورت ترد-ایمن
     */
    public void reportTaskCompleted(String taskId) {
        completedTasks.add(taskId);
    }

    /**
     * دریافت لیست تسک‌های تکمیل‌شده
     */
    public List<String> getCompletedTasks() {
        synchronized (completedTasks) {
            return new ArrayList<>(completedTasks);
        }
    }

    /**
     * اعلام خروج نهایی مسافر از ساختمان
     */
    public void passengerExitedBuilding(Passenger passenger) {
        exitedPassengersCount.incrementAndGet();
    }

    /**
     * تعداد مسافرانی که تا کنون از ساختمان خارج شده‌اند
     */
    public int getExitedPassengersCount() {
        return exitedPassengersCount.get();
    }
}
