package socialnetwork.domain.mainDom;

import socialnetwork.domain.Entity;
import socialnetwork.domain.Status;
import socialnetwork.domain.Tuple;

import java.time.LocalDateTime;

public class FriendRequest extends Entity<Tuple<Long, Long>> {

    private Status status;
    private LocalDateTime date;

    public FriendRequest(Status status, LocalDateTime date) {
        this.status = status;
        this.date = date;
    }

    public Status getStatus() {
        return status;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "status: " + status +
                " | date: " + date;
    }
}
