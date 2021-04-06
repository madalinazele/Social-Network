package socialnetwork.service;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import socialnetwork.domain.dto.NotificationDTO;
import socialnetwork.utils.ChangeEvent;
import socialnetwork.utils.observer.Observer;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class NotificationsHandler implements Observer<ChangeEvent<NotificationDTO>> {

    NotificationService notificationService;
    Timeline deleteNotifications;
    List<NotificationDTO> allNotifications;

    public NotificationsHandler(NotificationService notificationService) {
        this.notificationService = notificationService;
        this.notificationService.addObserver(this);

        setAllNotifications();

        deleteNotifications = new Timeline(new KeyFrame(Duration.millis(1), event -> {
            for (NotificationDTO n : allNotifications) {
                if (n.getDate().until(LocalDateTime.now(), ChronoUnit.HOURS) >= 24) {
                    notificationService.deleteNotification(n.getId());
                    //setAllNotifications();
                    break;
                }
            }
        }));
        deleteNotifications.setCycleCount(Timeline.INDEFINITE);
        deleteNotifications.play();
    }

    public void setAllNotifications() {
        allNotifications = notificationService.getAllNotifications();
    }

    @Override
    public void update(ChangeEvent<NotificationDTO> changeEvent) {
        setAllNotifications();
    }
}
