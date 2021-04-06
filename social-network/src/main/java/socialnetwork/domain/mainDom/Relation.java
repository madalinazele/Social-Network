package socialnetwork.domain.mainDom;

import socialnetwork.domain.Entity;
import socialnetwork.domain.Tuple;

import java.time.LocalDateTime;


public class Relation extends Entity<Tuple<Long, Long>> {

    private LocalDateTime date;

    public Relation(LocalDateTime date) {
        this.date = date;
    }

    /**
     * @return the date when the friendship was created
     */
    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

}
