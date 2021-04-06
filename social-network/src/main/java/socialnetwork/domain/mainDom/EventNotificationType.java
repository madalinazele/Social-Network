package socialnetwork.domain.mainDom;

import java.time.temporal.ChronoUnit;
import java.util.Objects;

public class EventNotificationType {
    private Long unit;
    private ChronoUnit chronoUnit;

    public EventNotificationType(Long unit, ChronoUnit chronoUnit) {
        this.unit = unit;
        this.chronoUnit = chronoUnit;
    }

    public Long getUnit() {
        return unit;
    }

    public void setUnit(Long unit) {
        this.unit = unit;
    }

    public ChronoUnit getChronoUnit() {
        return chronoUnit;
    }

    public void setChronoUnit(ChronoUnit chronoUnit) {
        this.chronoUnit = chronoUnit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EventNotificationType)) return false;
        EventNotificationType that = (EventNotificationType) o;
        return Objects.equals(getUnit(), that.getUnit()) && getChronoUnit() == that.getChronoUnit();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUnit(), getChronoUnit());
    }
}
