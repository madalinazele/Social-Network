package socialnetwork.domain.dto;

import socialnetwork.domain.mainDom.User;

import java.time.LocalDateTime;

public class FriendsDTO {
    private User user;
    private LocalDateTime date;

    public FriendsDTO(User user, LocalDateTime date) {
        this.user = user;
        this.date = date;
    }

    public User getUser() {
        return user;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "FriendsDTO{" +
                "user=" + user +
                " |Date: " + date;
    }
}
