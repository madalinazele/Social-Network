package socialnetwork.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import socialnetwork.domain.mainDom.User;
import socialnetwork.service.*;

import java.io.IOException;

public class HomeController {

    UserService userService;
    GroupService groupService;
    EventService eventService;
    NotificationService notificationService;
    User loggedUser;
    NotifyEvents notifyEvents;
    NotificationsHandler notificationsHandler;

    @FXML
    private AnchorPane mainPane;
    @FXML
    private Label nameLbl;
    @FXML
    private Button logOutBtn;

    private AnchorPane homePane, groupPane, notificationsPane, eventsPane, searchPane;

    public void setService(UserService userService, GroupService groupService,EventService eventService, NotificationService notificationService, User loggedUser) throws IOException {
        this.userService = userService;
        this.groupService = groupService;
        this.eventService = eventService;
        this.notificationService = notificationService;
        this.loggedUser = loggedUser;
        this.notifyEvents = new NotifyEvents(eventService,loggedUser);
        this.notificationsHandler = new NotificationsHandler(notificationService);

        nameLbl.setText(loggedUser.getFirstName() + " " + loggedUser.getLastName());

        FXMLLoader loader=new FXMLLoader();
        loader.setLocation(getClass().getResource("/view/events.fxml"));
        eventsPane = loader.load();
        EventsController ctrl=loader.getController();
        ctrl.setService(userService, eventService, loggedUser);

        loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/view/notifications.fxml"));
        notificationsPane = loader.load();
        NotificationsController ctrlN = loader.getController();
        ctrlN.setService(loggedUser, notificationService);

        loader=new FXMLLoader();
        loader.setLocation(getClass().getResource("/view/search.fxml"));
        searchPane = loader.load();
        SearchController ctrlS= loader.getController();
        ctrlS.setService(userService, loggedUser);

        loader=new FXMLLoader();
        loader.setLocation(getClass().getResource("/view/groupChat.fxml"));
        groupPane = loader.load();
        GroupsController ctrlG=loader.getController();
        ctrlG.setService(userService, groupService, loggedUser);

        loader=new FXMLLoader();
        loader.setLocation(getClass().getResource("/view/homePage.fxml"));
        homePane = loader.load();
        HomePageController ctrlH=loader.getController();
        ctrlH.setService(userService, groupService,eventService, notificationService, loggedUser);

        HomeBtnClicked();
    }

    public void EventsBtnClicked() {
        mainPane.setVisible(true);
        mainPane.getChildren().clear();
        mainPane.getChildren().add(eventsPane);
    }

    public void NotificationsBtnClicked() {
        mainPane.setVisible(true);
        mainPane.getChildren().clear();
        mainPane.getChildren().add(notificationsPane);
    }

    public void SearchBtnClicked() {

        mainPane.setVisible(true);
        mainPane.getChildren().clear();
        mainPane.getChildren().add(searchPane);
    }

    public void ChatBtnClicked() {

        mainPane.setVisible(true);
        mainPane.getChildren().clear();
        mainPane.getChildren().add(groupPane);
    }

    public void HomeBtnClicked() {

        mainPane.setVisible(true);
        mainPane.getChildren().clear();
        mainPane.getChildren().add(homePane);
    }

    public void LogOutBtnClicked() throws IOException {

        notifyEvents.stopTimeline();

        FXMLLoader loader=new FXMLLoader();
        loader.setLocation(getClass().getResource("/view/login.fxml"));
        AnchorPane pane = loader.load();
        LoginController ctrl=loader.getController();
        ctrl.setService(userService, groupService, eventService, notificationService);

        Stage mainStage = new Stage();
        mainStage.initStyle(StageStyle.TRANSPARENT);
        Scene scene = new Scene(pane);
        mainStage.setScene(scene);
        mainStage.show();

        Stage st = (Stage)logOutBtn.getScene().getWindow();
        st.close();
    }

    public void exitBtnClicked() {
        Stage st = (Stage)logOutBtn.getScene().getWindow();
        st.close();
    }

}
