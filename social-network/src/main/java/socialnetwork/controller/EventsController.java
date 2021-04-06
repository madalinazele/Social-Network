package socialnetwork.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import socialnetwork.domain.mainDom.Event;
import socialnetwork.domain.mainDom.User;
import socialnetwork.service.EventService;
import socialnetwork.service.UserService;
import socialnetwork.utils.ChangeEvent;
import socialnetwork.utils.ChangeEventType;
import socialnetwork.utils.Constants;
import socialnetwork.utils.observer.Observer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventsController implements Observer<ChangeEvent<Event>> {

    UserService userService;
    EventService eventService;
    User loggedUser;
    Map<Long, HBox> allEvents = new HashMap<>();
    Map<Long, HBox> myEvents = new HashMap<>();

    @FXML
    private Button allEventsBtn, myEventsBtn;
    @FXML
    private Label noAllEventsLbl, noMyEventsLbl;
    @FXML
    private AnchorPane myEventsPane, allEventsPane;
    @FXML
    private Pagination allEventsPaged, myEventsPaged;

    public void setService(UserService userService, EventService eventService, User loggedUser) {
        this.userService = userService;
        this.eventService = eventService;
        this.loggedUser = loggedUser;
        eventService.addObserver(this);
        this.eventService.setPagedSize(3);
        showAllEvents();
    }

    public void initModel() {
        initAllEvents();
        initMyEvents();
    }

    public HBox createProfileBox(HBox hbox, Event event) {
        hbox.getChildren().clear();

        hbox.setStyle("-fx-background-color: #000328; -fx-border-width: 10; -fx-border-color: #000328;");
        hbox.setMinSize(460, 80);
        hbox.setMaxSize(460, 80);
        Image pic = new Image("view/images/event-icon.png", 60, 60, false, false);
        ImageView profilePic = new ImageView(pic);
        hbox.getChildren().add(profilePic);

        VBox vBox = new VBox();
        vBox.setMaxWidth(130);
        vBox.setMinWidth(130);

        Label title = new Label(event.getTitle());
        title.setStyle("-fx-font-family: Segoe UI; -fx-font-weight: bold; -fx-font-size: 14; -fx-text-alignment: center; -fx-text-fill: #ffffff;");
        Label date = new Label(event.getDate().toString());
        date.setStyle("-fx-font-family: Segoe UI; -fx-font-weight: bold; -fx-font-size: 12; -fx-text-fill: #ffffff;");
        Label description = new Label(event.getDescription());
        description.setStyle("-fx-font-family: Segoe UI; -fx-font-style: italic ; -fx-font-size: 12;-fx-text-fill: #ffffff;");
        vBox.getChildren().addAll(title, date, description);

        hbox.getChildren().add(vBox);

        VBox box = new VBox();
        HBox leaveHBox = new HBox();
        Button button = new Button();
        button.setStyle("-fx-background-color: #27496d; -fx-text-fill: #ffffff;");

        if (event.getDate().until(LocalDateTime.now(), ChronoUnit.MINUTES) >= 0) {
            Label lbl = new Label("Event finished!");
            lbl.setStyle("-fx-font-family: Segoe UI; -fx-font-style: italic ; -fx-font-size: 14; -fx-text-fill: #ffffff;");
            leaveHBox.getChildren().add(lbl);
            HBox.setHgrow(leaveHBox, Priority.ALWAYS);
        } else if (event.getParticipants().containsKey(loggedUser.getId())) {
            if (event.getOrganizer().equals(loggedUser.getId())) {
                button = new Button("Organizer");
                button.setStyle("-fx-background-color: #27496d; -fx-text-fill: #ffffff;");
                ImageView starPic = new ImageView(new Image("/view/images/icons8_star_60px.png", 13, 13, false, false));
                button.setGraphic(starPic);
            } else {
                button.setText("Leave");
                button.setOnAction(ev -> {
                    eventService.removeParticipant(event, loggedUser);
                    createProfileBox(allEvents.get(event.getId()), event);
                });

            }
            leaveHBox.getChildren().add(button);

            Button notification = new Button();
            if (!event.getParticipants().get(loggedUser.getId())) {
                notification.setText("subscribe to notifications");
                notification.setOnAction(x -> eventService.subscribeToNotifications(event, loggedUser));
            } else {
                notification.setText("unsubscribe");
                notification.setOnAction(x -> eventService.unsubscribeToNotifications(event, loggedUser));
            }

            notification.setStyle("-fx-background-color: #27496d; -fx-text-fill: #ffffff;");
            leaveHBox.getChildren().add(notification);

            HBox.setHgrow(button, Priority.ALWAYS);
            HBox.setHgrow(notification, Priority.ALWAYS);
        } else {
            button.setText("Join");
            button.setOnAction(ev -> {
                eventService.addParticipant(event, loggedUser);
                createProfileBox(allEvents.get(event.getId()), event);
            });
            //button.setOnAction((x) -> eventService.addParticipant(event, loggedUser));
            leaveHBox.getChildren().add(button);
            HBox.setHgrow(button, Priority.ALWAYS);
        }

        box.getChildren().add(leaveHBox);
        VBox.setVgrow(leaveHBox, Priority.ALWAYS);

        Button showPart = new Button("Show participants");
        showPart.setStyle("-fx-background-color: #27496d; -fx-text-fill: #ffffff;");
        showPart.setOnAction((x) -> showParticipants(event));
        box.getChildren().add(showPart);
        VBox.setVgrow(showPart, Priority.ALWAYS);
        box.setSpacing(10);

        hbox.getChildren().add(box);
        hbox.setSpacing(20);
        HBox.setHgrow(vBox, Priority.ALWAYS);
        HBox.setHgrow(box, Priority.ALWAYS);
        hbox.setAlignment(Pos.BOTTOM_RIGHT);

        return hbox;
    }

    public void initAllEvents() {
        noAllEventsLbl.setVisible(false);
        allEventsPaged.setPageCount(eventService.getNumberOfPages(loggedUser.getId(), "allEvents"));
        allEventsPaged.setPageFactory(this::createPage);
    }

    public void initMyEvents() {
        noMyEventsLbl.setVisible(false);
        myEventsPaged.setPageCount(eventService.getNumberOfPages(loggedUser.getId(), "myEvents"));
        myEventsPaged.setPageFactory(this::createPageMyEvents);
    }

    public VBox createPage(Integer pageIndex) {
        List<Event> events = eventService.getEventsOnPage(pageIndex, loggedUser.getId(), "allEvents");
        VBox allEventsVBox = new VBox();
        allEvents.clear();

        if (events.size() == 0) {
            noAllEventsLbl.setText("No events!");
            noAllEventsLbl.setAlignment(Pos.CENTER);
            noAllEventsLbl.setVisible(true);
            allEventsVBox.getChildren().add(noAllEventsLbl);
        } else {
            for (Event ev : events) {
                HBox hBox = new HBox();
                createProfileBox(hBox, ev);

                allEventsVBox.getChildren().add(hBox);
                allEvents.put(ev.getId(), hBox);
            }
        }
        return allEventsVBox;
    }

    public VBox createPageMyEvents(Integer pageIdx) {
        List<Event> events = eventService.getEventsOnPage(pageIdx, loggedUser.getId(), "myEvents");
        VBox myEventsVBox = new VBox();

        myEvents.clear();
        if (events.size() == 0) {
            noMyEventsLbl.setText("No events");
            noMyEventsLbl.setAlignment(Pos.CENTER);
            myEventsVBox.getChildren().add(noMyEventsLbl);
            noMyEventsLbl.setVisible(true);
        }
        for (Event ev : events) {
            HBox hBox = new HBox();
            hBox = createProfileBox(hBox, ev);

            myEventsVBox.getChildren().add(hBox);
            myEvents.put(ev.getId(), hBox);
        }
        return myEventsVBox;
    }

    public void showAllEvents() {
        allEventsBtn.setStyle(Constants.selectedStyle);
        myEventsBtn.setStyle(Constants.btnStyle);

        initAllEvents();
        allEventsPane.setVisible(true);
        myEventsPane.setVisible(false);
    }

    public void showMyEvents() {
        myEventsBtn.setStyle(Constants.selectedStyle);
        allEventsBtn.setStyle(Constants.btnStyle);

        initMyEvents();
        allEventsPane.setVisible(false);
        myEventsPane.setVisible(true);
    }

    public void showParticipants(Event event) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Participants");
        alert.setHeaderText("Participants: ");
        if (event.getParticipants().size() == 0)
            alert.setContentText("There are no participants to this event!");
        else {
            VBox box = new VBox();
            ListView<String> participants = new ListView<>();

            for (Map.Entry<Long, Boolean> x : event.getParticipants().entrySet()) {
                User user = userService.getUser(x.getKey());
                participants.getItems().add(user.getFirstName() + " " + user.getLastName());
            }
            //participants.setMaxHeight(40);
            box.getChildren().addAll(participants);
            alert.getDialogPane().setContent(participants);
        }
        alert.showAndWait();
    }

    public void createEventClicked() throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/view/createEventForm.fxml"));
        AnchorPane pane = loader.load();
        AddEventWindow ctrl = loader.getController();
        ctrl.setService(eventService, loggedUser);

        Stage mainStage = new Stage();
        mainStage.initStyle(StageStyle.TRANSPARENT);
        Scene scene = new Scene(pane);
        mainStage.setScene(scene);
        mainStage.show();
    }

    @Override
    public void update(ChangeEvent<Event> event) {
        if (event.getType() == ChangeEventType.JOINEVENT || event.getType() == ChangeEventType.LEAVEEVENT)
            initMyEvents();

        else if (event.getType() == ChangeEventType.UPDATE) {
            createProfileBox(allEvents.get(event.getData().getId()), event.getData());
            if (myEvents.containsKey(event.getData().getId()))
                createProfileBox(myEvents.get(event.getData().getId()), event.getData());
        } else initModel();

    }
}
