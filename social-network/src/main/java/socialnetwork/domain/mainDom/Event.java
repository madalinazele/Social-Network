package socialnetwork.domain.mainDom;

import javafx.util.Pair;
import socialnetwork.domain.Entity;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Objects;

public class Event extends Entity<Long> {
    private String title;
    private String description;
    private LocalDateTime date;
    private Long organizer;
    private HashMap<Long, Boolean> participants;
    private String imageUrl;


    public Event(String title, String description, LocalDateTime date, Long organizer, String imageUrl) {
        this.title = title;
        this.description = description;
        this.date = date;
        this.organizer = organizer;
        this.imageUrl = imageUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public HashMap<Long, Boolean> getParticipants() {
        return participants;
    }

    public void setParticipants(HashMap<Long, Boolean> participants) {
        this.participants = participants;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void addParticipant(Pair<Long, Boolean> member) {
        participants.put(member.getKey(), member.getValue());
    }

    public void removeParticipant(Long idUser) {
        participants.remove(idUser);
    }

    @Override
    public String toString() {
        return "Event{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", date=" + date +
                ", participants=" + participants +
                ", imageUrl='" + imageUrl + '\'' +
                '}';
    }

    public Long getOrganizer() {
        return organizer;
    }

    public void setOrganizer(Long organizer) {
        this.organizer = organizer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Event)) return false;
        Event event = (Event) o;
        return Objects.equals(getId(), event.getId()) && Objects.equals(getTitle(), event.getTitle()) && Objects.equals(getDescription(), event.getDescription()) && Objects.equals(getDate(), event.getDate()) && Objects.equals(getOrganizer(), event.getOrganizer()) && Objects.equals(getParticipants(), event.getParticipants()) && Objects.equals(getImageUrl(), event.getImageUrl());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTitle(), getDescription(), getDate(), getOrganizer(), getParticipants(), getImageUrl());
    }
}
