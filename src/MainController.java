import environment.Building;
import environment.FairnessMode;
import models.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class MainController {
    private static MainController instance;
    private Building building;
    private final List<Elevator> elevators = new ArrayList<>();
    private final List<Passenger> passengers = new ArrayList<>();
    private final List<Thread> threads = new ArrayList<>();

    private MainController() {}

    public static synchronized MainController getInstance() {
        if (instance == null) {
            instance = new MainController();
        }
        return instance;
    }

    public static void main(String[] args) {
        MainController controller = MainController.getInstance();
        controller.runSimulation();
    }

    public void runSimulation() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("=== Sharif University Elevator Simulation ===");
        System.out.print("Enter number of passengers (n): ");
        int numPassengers = scanner.hasNextInt() ? scanner.nextInt() : 10;
        System.out.print("Enter number of elevators (m): ");
        int numElevators = scanner.hasNextInt() ? scanner.nextInt() : 3;
        System.out.print("Enter number of floors: ");
        int numFloors = scanner.hasNextInt() ? scanner.nextInt() : 8;

        System.out.println("\nSelect Fairness Mode:");
        System.out.println("1. Task Priority (Default)");
        System.out.println("2. Role / Rank Priority");
        System.out.println("3. Age Priority (Respect for Elders)");
        System.out.println("4. Advanced Combined Fairness with Aging (Bonus)");
        System.out.print("Choice (1-4): ");
        int fairnessChoice = scanner.hasNextInt() ? scanner.nextInt() : 4;
        FairnessMode mode;
        switch (fairnessChoice) {
            case 2:
                mode = FairnessMode.ROLE_RANK;
                break;
            case 3:
                mode = FairnessMode.AGE;
                break;
            case 4:
                mode = FairnessMode.ADVANCED;
                break;
            case 1:
            default:
                mode = FairnessMode.TASK_PRIORITY;
                break;
        }

        System.out.println("\nStarting simulation with " + mode + " fairness mode...\n");

        long startTime = System.currentTimeMillis();
        building = new Building(numFloors, mode, 2500);
        Random random = new Random();

        // 1. Create and Register Elevators using Factory Method
        for (int i = 0; i < numElevators; i++) {
            Elevator elevator = createElevator(i, building);
            elevators.add(elevator);
            building.addElevator(elevator);

            Thread t = new Thread(elevator, "Thread-" + elevator.getId());
            threads.add(t);
            t.start();
        }

        // 2. Create Passengers using Factory Method
        for (int i = 0; i < numPassengers; i++) {
            Passenger p = createPassenger(i, numFloors, building, random);
            passengers.add(p);

            Thread t = new Thread(p, "Thread-" + p.getId());
            threads.add(t);
            t.start();
        }

        // 3. Graceful Wait until ALL passengers have finished tasks AND returned to ground floor
        while (building.getExitedPassengersCount() < numPassengers) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        System.out.println("\n--- ALL PASSENGERS COMPLETED THEIR TASKS AND LEFT THE BUILDING ---");

        // 4. Graceful Shutdown
        for (Passenger p : passengers) {
            p.stop();
        }
        for (Elevator e : elevators) {
            e.stop();
        }

        for (Thread t : threads) {
            t.interrupt();
        }

        long endTime = System.currentTimeMillis();
        long totalActiveTime = endTime - startTime;

        // 5. Final Report
        System.out.println("================ SIMULATION REPORT ================");
        System.out.println("Total Passengers: " + numPassengers);
        System.out.println("Total Completed Tasks: " + building.getCompletedTasks().size());
        System.out.println("Completed Task IDs: " + building.getCompletedTasks());
        System.out.println("Max Global System Weight Allowed: " + building.getMaxSystemWeight() + " kg");
        System.out.println("Total Simulation Duration: " + totalActiveTime + " ms");
        System.out.println("====================================================");
    }

    /**
     * متد کارخانه‌ای (Factory Method) برای ایجاد آسانسور بر اساس ایندکس و نوع
     */
    private Elevator createElevator(int index, Building building) {
        if (index == 0) {
            return new PublicElevator("E" + (index + 1) + "-Public", 1000, building);
        } else if (index == 1) {
            return new VipElevator("E" + (index + 1) + "-VIP", 800, building);
        } else if (index == 2) {
            return new FreightElevator("E" + (index + 1) + "-Freight", 2000, building);
        } else {
            return new PublicElevator("E" + (index + 1) + "-Public", 1000, building);
        }
    }

    /**
     * متد کارخانه‌ای (Factory Method) برای ایجاد مسافر تصادفی با تسک اختصاصی
     */
    private Passenger createPassenger(int index, int numFloors, Building building, Random random) {
        int destFloor = random.nextInt(numFloors - 1) + 1;
        Priority prio = random.nextBoolean() ? Priority.HIGH : (random.nextBoolean() ? Priority.MEDIUM : Priority.LOW);
        Task task = new Task("T" + (index + 1), destFloor, 800 + random.nextInt(500), prio);

        int roleChance = random.nextInt(10);
        if (roleChance < 2) {
            return new Professor("Prof-" + (index + 1), 50 + random.nextInt(25), 80, task, 0, building);
        } else if (roleChance < 4) {
            return new Deputy("Dep-" + (index + 1), 40 + random.nextInt(25), 75, task, 0, building);
        } else if (roleChance < 6) {
            return new Porter("Port-" + (index + 1), 25 + random.nextInt(20), 85, 100 + random.nextInt(150), task, 0, building);
        } else {
            return new Student("Stu-" + (index + 1), 18 + random.nextInt(10), 70, task, 0, building);
        }
    }
}
