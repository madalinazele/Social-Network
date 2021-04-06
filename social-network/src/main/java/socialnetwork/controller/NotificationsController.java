package socialnetwork.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Pagination;
import socialnetwork.domain.dto.NotificationDTO;
import socialnetwork.domain.mainDom.User;
import socialnetwork.domain.validators.ValidationException;
import socialnetwork.service.NotificationService;
import socialnetwork.utils.ChangeEvent;
import socialnetwork.utils.observer.Observer;

import javax.swing.*;

public class NotificationsController implements Observer<ChangeEvent<NotificationDTO>> {
    User loggedUser;
    NotificationService notificationService;

    @FXML
    private Pagination notificationsPaged;
    @FXML
    private Label noNotificationBtn;

    public void setService(User loggedUser, NotificationService notificationService) {
        this.loggedUser = loggedUser;
        this.notificationService = notificationService;
        notificationService.addObserver(this);
        notificationService.setPageSize(5);
        initModel();
    }

    public void initModel() {
        noNotificationBtn.setVisible(false);
        notificationsPaged.setPageCount(notificationService.getNumberOfPages(loggedUser.getId(), "allNotifications"));
        notificationsPaged.setPageFactory(pageIdx -> {
            ListView<String> notificationsList = new ListView<>();
            notificationsList.setStyle("-fx-font-family: Segoe UI; -fx-font-weight: bold; -fx-font-size: 13; -fx-text-alignment: left; -fx-background-color: #000328");

            notificationsList.setStyle("-fx-control-inner-background: #000328; -fx-border-color: #000328");
            notificationService.getNotificationsOnPage(pageIdx, loggedUser.getId(), "allNotifications")
                    .forEach(x -> notificationsList.getItems().add(notificationService.getNotificationMessage(x)));
            notificationsList.setFixedCellSize(44);

            if (notificationsList.getItems().size() == 0) {
                noNotificationBtn.setVisible(true);
                return noNotificationBtn;
            }
            return notificationsList;
        });
    }

    public void clearAllBtnClicked() {
        try {
            notificationService.deleteAllUserNotifications(loggedUser);
        } catch (ValidationException e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
    }

    @Override
    public void update(ChangeEvent<NotificationDTO> changeEvent) {
        initModel();
    }
}
