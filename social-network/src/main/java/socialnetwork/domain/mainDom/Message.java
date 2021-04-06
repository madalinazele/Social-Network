package socialnetwork.domain.mainDom;

import socialnetwork.domain.Entity;

import java.time.LocalDateTime;

public class Message extends Entity<Long> {
    private User from;
    private Group to;
    private String message;
    private LocalDateTime date;
    private Long reply;

    public Message(User from, Group to, String message, LocalDateTime date) {
        this.from = from;
        this.to = to;
        this.message = message;
        this.date = date;
    }

    public Message(User from, Group to, String message, LocalDateTime date, Long reply) {
        this.from = from;
        this.to = to;
        this.message = message;
        this.date = date;
        this.reply = reply;
    }

    public User getFrom() {
        return from;
    }

    public Group getTo() {
        return to;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getDate() {
        return date;
    }

    @Override
    public String toString() {
        return "Id: " + getId() +
                " |From: " + from.getFirstName() + " " + from.getLastName() +
                " |Message: " + message +
                " |Date: " + date;
    }

    public void setFrom(User from) {
        this.from = from;
    }

    public void setTo(Group to) {
        this.to = to;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public Long getReply() {
        return reply;
    }

    public void setReply(Long reply) {
        this.reply = reply;
    }
}
