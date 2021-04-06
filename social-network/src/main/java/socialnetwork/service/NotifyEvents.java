package socialnetwork.service;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import javafx.util.Pair;
import socialnetwork.domain.mainDom.Event;
import socialnetwork.domain.mainDom.EventNotificationType;
import socialnetwork.domain.mainDom.User;
import socialnetwork.utils.ChangeEvent;
import socialnetwork.utils.observer.Observer;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class NotifyEvents implements Observer<ChangeEvent<Event>> {

    EventService eventService;
    User loggedUser;
    List<Event> allEvents = new ArrayList<>();
    //Map<Event, EventNotificationType> alreadyNotified = new HashMap<>();
    List<Pair<Event, EventNotificationType>> alreadyNotified = new ArrayList<>();
    List<Pair<Event, EventNotificationType>> currentNotifications = new ArrayList<>();
    Timeline verifyEvents;
    List<EventNotificationType> notifyIntervals = new ArrayList<>();

    public NotifyEvents(EventService eventService, User loggedUser) {
        this.eventService = eventService;
        this.loggedUser = loggedUser;
        this.eventService.addObserver(this);

        notifyIntervals.add(new EventNotificationType(0L, ChronoUnit.MINUTES));
        notifyIntervals.add(new EventNotificationType(1L, ChronoUnit.MINUTES));
        notifyIntervals.add(new EventNotificationType(5L, ChronoUnit.MINUTES));
        notifyIntervals.add(new EventNotificationType(1L, ChronoUnit.HOURS));
        notifyIntervals.add(new EventNotificationType(1L, ChronoUnit.DAYS));

        setAllEvents();

        this.verifyEvents = new Timeline(new KeyFrame(Duration.minutes(0.5), event -> {
            for (Event e : allEvents) {
                if (e.getDate().isAfter(LocalDateTime.now())) {
                    for (EventNotificationType notifyTime : notifyIntervals) {

                        Pair<Event, EventNotificationType> eventNotification = new Pair<>(e, notifyTime);

                        if ((!verifyAlreadyNotified(eventNotification)) && LocalDateTime.now().until(e.getDate(), notifyTime.getChronoUnit()) == notifyTime.getUnit()) {
                            currentNotifications.add(eventNotification);
                            alreadyNotified.add(eventNotification);
                            eventService.createEventNotification(e, notifyTime, loggedUser);
                        }
                    }
                }
            }
        }
        ));
        verifyEvents.setCycleCount(Timeline.INDEFINITE);
        verifyEvents.play();
    }

    private boolean verifyAlreadyNotified(Pair<Event, EventNotificationType> eventNotification) {
        for (Pair<Event, EventNotificationType> x : alreadyNotified) {
            if (x.getKey().getId().equals(eventNotification.getKey().getId()) && x.getValue().equals(eventNotification.getValue()))
                return true;
        }
        return false;
    }

    public void stopTimeline() {
        verifyEvents.stop();
    }

    public void setAllEvents() {
        allEvents = eventService.getAllNotifiableEvents(loggedUser.getId());
    }

    public List<Pair<Event, EventNotificationType>> getEventNotifications() {
        return currentNotifications;
    }

    @Override
    public void update(ChangeEvent<Event> changeEvent) {
        setAllEvents();
    }
}
