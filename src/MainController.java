import environment.Building;
import models.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class MainController {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("=== Elevator Simulation ===");
        System.out.print("Enter number of passengers (n): ");
        int numPassengers = scanner.nextInt();
        System.out.print("Enter number of elevators (m): ");
        int numElevators = scanner.nextInt();
        System.out.print("Enter number of floors: ");
        int numFloors = scanner.nextInt();
        System.out.println("Starting simulation...\n");

        long startTime = System.currentTimeMillis();

        Building building = new Building(numFloors);
        Random random = new Random();

        List<Thread> threads = new ArrayList<>();
        List<Elevator> elevators = new ArrayList<>();
        List<Passenger> passengers = new ArrayList<>();

        // Create Elevators
        for (int i = 0; i < numElevators; i++) {
            if (i == 0) {
                elevators.add(new PublicElevator("E" + (i+1) + "-Public", 1000, building));
            } else if (i == 1) {
                elevators.add(new VipElevator("E" + (i+1) + "-VIP", 800, building));
            } else if (i == 2) {
                elevators.add(new FreightElevator("E" + (i+1) + "-Freight", 2000, building));
            } else {
                elevators.add(new PublicElevator("E" + (i+1) + "-Public", 1000, building));
            }
        }

        for (Elevator e : elevators) {
            Thread t = new Thread(e);
            threads.add(t);
            t.start();
        }

        // Create Mechanics (Bonus)
        Mechanic mech1 = new Mechanic("Mech-1", 45, 80, 0, building);
        passengers.add(mech1);
        Thread mt = new Thread(mech1);
        threads.add(mt);
        mt.start();

        // Create Passengers
        for (int i = 0; i < numPassengers; i++) {
            int destFloor = random.nextInt(numFloors - 1) + 1;
            Priority prio = random.nextBoolean() ? Priority.HIGH : (random.nextBoolean() ? Priority.MEDIUM : Priority.LOW);
            Task task = new Task("T" + (i+1), destFloor, 1000, prio);
            
            Passenger p;
            int roleChance = random.nextInt(10);
            if (roleChance < 2) {
                p = new Professor("Prof-" + (i+1), 50 + random.nextInt(20), 80, task, 0, building);
            } else if (roleChance < 4) {
                p = new Deputy("Dep-" + (i+1), 40 + random.nextInt(20), 75, task, 0, building);
            } else if (roleChance < 6) {
                p = new Porter("Port-" + (i+1), 30 + random.nextInt(15), 85, 100 + random.nextInt(200), task, 0, building);
            } else {
                p = new Student("Stu-" + (i+1), 18 + random.nextInt(10), 70, task, 0, building);
            }
            passengers.add(p);
            
            Thread t = new Thread(p);
            threads.add(t);
            t.start();
        }

        // Wait for all non-mechanic passengers to complete
        while (building.getCompletedTasks().size() < numPassengers) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        System.out.println("--- ALL PASSENGERS COMPLETED THEIR TASKS, SHUTTING DOWN ---");

        for (Passenger p : passengers) p.stop();
        for (Elevator e : elevators) e.stop();
        
        for (Thread t : threads) {
            t.interrupt();
        }

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;

        System.out.println("Total Completed Tasks: " + building.getCompletedTasks().size());
        System.out.println("Completed Tasks IDs: " + building.getCompletedTasks());
        System.out.println("Total System Active Time (ms): " + totalTime);
    }
}
