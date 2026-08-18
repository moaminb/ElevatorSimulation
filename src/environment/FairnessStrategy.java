package environment;

import java.util.List;
import models.Elevator;
import models.Passenger;

@FunctionalInterface
public interface FairnessStrategy {
    /**
     * انتخاب مسافر مناسب از لیست مسافران منتظر در صف بر اساس استراتژی عدالت
     */
    Passenger selectPassenger(List<Passenger> waiting, Elevator elevator);
}
