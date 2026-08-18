package environment;

import java.util.List;
import models.Elevator;
import models.Passenger;

public enum FairnessMode implements FairnessStrategy {
    TASK_PRIORITY {
        @Override
        public Passenger selectPassenger(List<Passenger> waiting, Elevator elevator) {
            Passenger best = null;
            for (Passenger p : waiting) {
                if (elevator.canAccept(p)) {
                    if (best == null || p.getTask().getPriority().getLevel() > best.getTask().getPriority().getLevel()) {
                        best = p;
                    }
                }
            }
            return best;
        }
    },
    ROLE_RANK {
        @Override
        public Passenger selectPassenger(List<Passenger> waiting, Elevator elevator) {
            Passenger best = null;
            for (Passenger p : waiting) {
                if (elevator.canAccept(p)) {
                    if (best == null || p.getRole().getRank() > best.getRole().getRank()) {
                        best = p;
                    }
                }
            }
            return best;
        }
    },
    AGE {
        @Override
        public Passenger selectPassenger(List<Passenger> waiting, Elevator elevator) {
            Passenger best = null;
            for (Passenger p : waiting) {
                if (elevator.canAccept(p)) {
                    if (best == null || p.getAge() > best.getAge()) {
                        best = p;
                    }
                }
            }
            return best;
        }
    },
    ADVANCED {
        @Override
        public Passenger selectPassenger(List<Passenger> waiting, Elevator elevator) {
            Passenger best = null;
            double bestScore = -1.0;
            for (Passenger p : waiting) {
                if (elevator.canAccept(p)) {
                    double taskScore = p.getTask().getPriority().getLevel() * 10.0;
                    double roleScore = p.getRole().getRank() * 5.0;
                    double ageScore = p.getAge() * 0.2;
                    double agingScore = (p.getWaitingTime() / 1000.0) * 0.5;

                    double totalScore = taskScore + roleScore + ageScore + agingScore;
                    if (best == null || totalScore > bestScore) {
                        best = p;
                        bestScore = totalScore;
                    }
                }
            }
            return best;
        }
    };
}
