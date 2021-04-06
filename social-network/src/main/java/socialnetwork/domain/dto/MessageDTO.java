package socialnetwork.domain.dto;

import socialnetwork.domain.Entity;

import java.time.LocalDateTime;

public class MessageDTO extends Entity<Long> {
    private long fromUser;
    private long toGroup;
    private String message;
    private LocalDateTime date;
    private Long reply;

    public MessageDTO(long fromUser, long toGroup, String msg, LocalDateTime date) {
        this.fromUser = fromUser;
        this.toGroup = toGroup;
        this.message = msg;
        this.date = date;
    }

    public MessageDTO(long fromUser, long toGroup, String message, LocalDateTime date, Long reply) {
        this.fromUser = fromUser;
        this.toGroup = toGroup;
        this.message = message;
        this.date = date;
        this.reply = reply;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getFromUser() {
        return fromUser;
    }

    public void setFromUser(long fromUser) {
        this.fromUser = fromUser;
    }

    public long getToGroup() {
        return toGroup;
    }

    public void setToGroup(long toGroup) {
        this.toGroup = toGroup;
    }

    @Override
    public String toString() {
        return "Id: " + getId() +
                " |From:" + fromUser +
                " |To: " + toGroup +
                " |Message: " + message +
                " |Date: " + date;
    }

    public Long getReply() {
        return reply;
    }

    public void setReply(Long reply) {
        this.reply = reply;
    }
}
