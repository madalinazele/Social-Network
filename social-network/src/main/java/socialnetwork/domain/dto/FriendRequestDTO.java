package socialnetwork.domain.dto;

import socialnetwork.domain.Entity;
import socialnetwork.domain.Status;
import socialnetwork.domain.Tuple;
import socialnetwork.domain.mainDom.User;

import java.time.LocalDateTime;

public class FriendRequestDTO extends Entity<Tuple<User, User>> {
    private Status status;
    private LocalDateTime date;

    public FriendRequestDTO(Status status, LocalDateTime date) {
        this.status = status;
        this.date = date;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public Status getStatus() {
        return status;
    }

    public LocalDateTime getDate() {
        return date;
    }
}
