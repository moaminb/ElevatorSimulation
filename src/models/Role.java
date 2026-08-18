package models;

public enum Role {
    DEPUTY(3),
    PROFESSOR(2),
    STUDENT(1),
    PORTER(1);

    private final int rank;

    Role(int rank) {
        this.rank = rank;
    }

    public int getRank() {
        return rank;
    }
}
