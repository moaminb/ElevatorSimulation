package models;

public class Task {
    private final String id;
    private final int targetFloor;
    private final long duration; // in milliseconds
    private final Priority priority;

    public Task(String id, int targetFloor, long duration, Priority priority) {
        this.id = id;
        this.targetFloor = targetFloor;
        this.duration = duration;
        this.priority = priority;
    }

    public String getId() { return id; }
    public int getTargetFloor() { return targetFloor; }
    public long getDuration() { return duration; }
    public Priority getPriority() { return priority; }
    
    @Override
    public String toString() {
        return "Task{" + "id='" + id + '\'' + ", floor=" + targetFloor + ", priority=" + priority + '}';
    }
}
