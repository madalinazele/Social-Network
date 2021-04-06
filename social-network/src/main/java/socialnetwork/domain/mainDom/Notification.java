package socialnetwork.domain.mainDom;

import socialnetwork.domain.Entity;

import java.time.LocalDateTime;

public class Notification extends Entity<Long> {
    private User to;
    private LocalDateTime date;
    private String type;
    private boolean seen;

    public Notification(User to, LocalDateTime date, String type, boolean seen) {
        this.to = to;
        this.date = date;
        this.type = type;
        this.seen = seen;
    }

    public boolean getSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public User getTo() {
        return to;
    }

    public void setTo(User to) {
        this.to = to;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public java.lang.String toString() {
        return ", to=" + to +
                ", date=" + date +
                ", type='" + type + '\'' +
                ", seen='" + seen +
                '}';
    }
}
