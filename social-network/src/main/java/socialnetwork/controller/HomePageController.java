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
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import socialnetwork.domain.Tuple;
import socialnetwork.domain.dto.FriendRequestDTO;
import socialnetwork.domain.dto.FriendsDTO;
import socialnetwork.domain.mainDom.User;
import socialnetwork.domain.validators.ValidationException;
import socialnetwork.service.EventService;
import socialnetwork.service.GroupService;
import socialnetwork.service.NotificationService;
import socialnetwork.service.UserService;
import socialnetwork.utils.ChangeEvent;
import socialnetwork.utils.Constants;
import socialnetwork.utils.PdfReports;
import socialnetwork.utils.observer.Observer;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class HomePageController implements Observer<ChangeEvent<User>> {
    UserService userService;
    GroupService groupService;
    EventService eventService;
    NotificationService notificationService;
    User loggedUser;
    private Map<String, Integer> months;
    private final Map<Long, HBox> friendsMap = new HashMap<>();

    @FXML
    private ImageView profileImage;
    @FXML
    private Label nameLbl, noFrLbl, noFrLblSent, noFriendsLbl;
    @FXML
    private TextField txtUpdateFirst, txtUpdateLast, txtUpdateUsername, txtUpdateEmail, searchBarFriends;
    @FXML
    private Button aboutBtn, friendsBtn, fRequestsBtn, deleteAccountBtn, fRequestBtn, sentFRBtn, pictureChooser;
    @FXML
    private PasswordField txtUpdatePassword;
    @FXML
    private AnchorPane accountInfoPane, friendsPane, frequestsPane, friendRPane, sentFrPane;
    @FXML
    private DatePicker datePicker1, datePicker2;
    @FXML
    private ChoiceBox<String> datePickerFriends;
    @FXML
    private Pagination receivedFrPaged, sentFrPaged, friendsPaging;

    public void setService(UserService userService, GroupService groupService, EventService eventService, NotificationService notificationService, User loggedUser) {
        this.userService = userService;
        this.groupService = groupService;
        this.eventService = eventService;
        this.notificationService = notificationService;
        this.loggedUser = loggedUser;
        userService.addObserver(this);
        userService.setPagedSize(3);

        nameLbl.setText(loggedUser.getFirstName() + " " + loggedUser.getLastName());
        initModel();
        initFR();
        initAbout();
        showAboutPane();
        searchBarFriends.textProperty().addListener((x) -> handleFilter());
        datePickerFriends.getSelectionModel().selectedItemProperty().addListener((x, oldValue, newValue) -> initModel());
    }

    @FXML
    public void initialize() {

        months = new HashMap<>();
        months.put("January", 1);
        months.put("February", 2);
        months.put("March", 3);
        months.put("April", 4);
        months.put("May", 5);
        months.put("June", 6);
        months.put("July", 7);
        months.put("August", 8);
        months.put("September", 9);
        months.put("October", 10);
        months.put("November", 11);
        months.put("December", 12);
        datePickerFriends.getItems().addAll("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December", "");
        datePickerFriends.setValue("");
    }

    private void initModel() {
        friendsPaging.setPageCount(userService.getNrFriendsPages(loggedUser.getId(), "friends"));
        friendsMap.clear();
        noFriendsLbl.setVisible(false);

        friendsPaging.setPageFactory(pageIdx -> {
            List<FriendsDTO> allFriends = userService.getRepoFriendsPage(pageIdx, loggedUser.getId(), "friends");
            VBox vBox = new VBox();

            for (FriendsDTO fr : allFriends) {
                HBox hbox = createProfileBox(fr, "friend");
                vBox.getChildren().add(hbox);
                friendsMap.put(fr.getUser().getId(), hbox);
            }
            return vBox;
        });
    }

    private void initFR() {
        initFriendRequests();
        initSentRequests();
    }

    public void initAbout() {
        Image image = new Image(loggedUser.getImage(), 60, 60, false, false);
        profileImage.setImage(image);
        txtUpdateFirst.setText(loggedUser.getFirstName());
        txtUpdateLast.setText(loggedUser.getLastName());
        txtUpdateUsername.setText(loggedUser.getUsername());
        txtUpdateEmail.setText(loggedUser.getEmail());
        txtUpdatePassword.setText(loggedUser.getPassword());
        //txtUpdateImage.setText(loggedUser.getImage());
    }

    public void handleFilter() {
        List<FriendsDTO> friends = getFriendsList()
                .stream()
                .filter(x -> x.getUser().getFirstName().contains(searchBarFriends.getText()) ||
                        x.getUser().getLastName().contains(searchBarFriends.getText()))
                .collect(Collectors.toList());

        friendsPaging.setPageCount(userService.getNrFriendsPagesFiltered(friends));
        friendsPaging.setPageFactory(pageIdx -> {
            List<FriendsDTO> friendsPage = userService.getFriendsPageFiltered(pageIdx, friends);
            VBox vBox = new VBox();

            for (FriendsDTO fr : friendsPage) {
                HBox hbox = friendsMap.get(fr.getUser().getId());
                vBox.getChildren().add(hbox);
            }
            return vBox;
        });
    }

    public HBox createProfileBox(FriendsDTO user, String type) {
        HBox hbox = new HBox();
        hbox.setStyle("-fx-background-color: #000328; -fx-border-width: 10; -fx-border-color: #000328;");
        hbox.setMinSize(450, 60);
        Image pic = new Image(user.getUser().getImage(), 60, 60, false, false);
        ImageView profilePic = new ImageView(pic);
        hbox.getChildren().add(profilePic);

        VBox vBox = new VBox();

        Label nume = new Label(user.getUser().getFirstName() + " " + user.getUser().getLastName());
        nume.setStyle("-fx-font-family: Segoe UI; -fx-font-weight: bold; -fx-font-size: 14; -fx-text-alignment: center; -fx-text-fill: #ffffff;");
        Label username = new Label(user.getUser().getUsername());
        username.setStyle("-fx-font-family: Segoe UI; -fx-font-weight: bold; -fx-font-size: 12; -fx-text-fill: #ffffff;");
        Label email = new Label(user.getUser().getEmail());
        email.setStyle("-fx-font-family: Segoe UI; -fx-font-style: italic ; -fx-font-size: 12;-fx-text-fill: #ffffff;");
        vBox.getChildren().addAll(nume, username, email);

        hbox.getChildren().add(vBox);

        VBox rightSide = new VBox();
        switch (type) {
            case "friend" -> {
                Label date = new Label("From: " + user.getDate().format(Constants.DATE_TIME_FORMATTER));
                date.setStyle("-fx-font-family: Segoe UI; -fx-font-style: italic ; -fx-font-size: 12; -fx-text-fill: #ffffff;");
                Button button = new Button("Remove");
                button.setStyle("-fx-background-color: #27496d; -fx-text-fill: #ffffff;");
                button.setOnAction((x) -> userService.deleteRelation(new Tuple<>(loggedUser.getId(), user.getUser().getId())));
                rightSide.getChildren().addAll(date, button);
                VBox.setVgrow(button, Priority.ALWAYS);
            }
            case "sent" -> {
                Button cancelBtn = new Button("Cancel friend request");
                cancelBtn.setStyle("-fx-background-color: #27496d; -fx-text-fill: #ffffff;");
                cancelBtn.setOnAction((x) -> userService.cancelFriendRequest(loggedUser.getId(), user.getUser().getId()));
                rightSide.getChildren().add(cancelBtn);
                VBox.setVgrow(cancelBtn, Priority.ALWAYS);
            }
            case "request" -> {
                Button acceptBtn = new Button("Accept");
                acceptBtn.setStyle("-fx-background-color: #27496d; -fx-text-fill: #ffffff;");
                acceptBtn.setOnAction((x) -> userService.respondFriendRequest(loggedUser.getId(), user.getUser().getId(), "yes"));
                rightSide.getChildren().add(acceptBtn);
                VBox.setVgrow(acceptBtn, Priority.ALWAYS);
                Button declineBtn = new Button("Decline");
                declineBtn.setStyle("-fx-background-color: #27496d; -fx-text-fill: #ffffff;");
                declineBtn.setOnAction((x) -> userService.respondFriendRequest(loggedUser.getId(), user.getUser().getId(), "no"));
                rightSide.getChildren().add(declineBtn);
                VBox.setVgrow(declineBtn, Priority.ALWAYS);
            }
        }

        rightSide.setAlignment(Pos.BOTTOM_RIGHT);
        rightSide.setSpacing(15);

        hbox.getChildren().add(rightSide);
        hbox.setSpacing(20);
        HBox.setHgrow(vBox, Priority.ALWAYS);
        HBox.setHgrow(rightSide, Priority.ALWAYS);
        hbox.setAlignment(Pos.BOTTOM_RIGHT);

        return hbox;
    }

    public void changePictureClicked() {
        Stage st = (Stage) pictureChooser.getScene().getWindow();
        FileChooser fc = new FileChooser();
        fc.setInitialDirectory(new File("C:/Users/Madalina/Desktop/social-network/src/main/resources/view/images/profile"));
        File selectedFile = fc.showOpenDialog(st);
        if (selectedFile != null) {
            Image img = new Image(selectedFile.toURI().toString(), 60, 60, false, false);
            try {
                userService.updateUser(loggedUser.getId(), loggedUser.getFirstName(), loggedUser.getLastName(), loggedUser.getEmail(),
                        loggedUser.getUsername(), loggedUser.getPassword(), img.getUrl());

                profileImage.setImage(img);
            } catch (ValidationException e) {
                JOptionPane.showMessageDialog(null, "Invalid picture!");
            }
        }
    }

    public List<FriendsDTO> getFriendsList() {
        if (!datePickerFriends.getValue().equals("")) {
            return userService.getFriendsDate(loggedUser.getId(), months.get(datePickerFriends.getValue()));
        }
        return userService.findFriends(loggedUser.getId());
    }

    public void initFriendRequests() {
        noFrLbl.setVisible(false);

        receivedFrPaged.setPageCount(userService.getNrFrPages(loggedUser.getId(), "received"));
        receivedFrPaged.setPageFactory(pageIdx -> {
            List<FriendRequestDTO> friendRequests = userService.getRepoFRPage(pageIdx, loggedUser.getId(), "received");
            VBox vBox = new VBox();

            if (friendRequests.size() == 0) {
                noFrLbl.setText("No friend request!");
                noFrLbl.setVisible(true);
                return noFrLbl;
            } else {
                for (FriendRequestDTO fr : friendRequests) {
                    HBox hBox = createProfileBox(new FriendsDTO(fr.getId().getLeft(), fr.getDate()), "request");
                    vBox.getChildren().add(hBox);
                }
            }
            return vBox;
        });
    }

    public void initSentRequests() {
        noFrLblSent.setVisible(false);
        sentFrPaged.setPageCount(userService.getNrFrPages(loggedUser.getId(), "sent"));
        sentFrPaged.setPageFactory(pageIdx -> {
            List<FriendRequestDTO> friendRequests = userService.getRepoFRPage(pageIdx, loggedUser.getId(), "sent");
            VBox vBox = new VBox();

            if (friendRequests.size() == 0) {
                noFrLblSent.setText("No friend request");
                noFrLblSent.setVisible(true);
                return noFrLblSent;
            }
            for (FriendRequestDTO fr : friendRequests) {
                HBox hBox = createProfileBox(new FriendsDTO(fr.getId().getRight(), fr.getDate()), "sent");
                vBox.getChildren().add(hBox);
            }
            return vBox;
        });
    }

    public void showFrRequests() {
        fRequestBtn.setStyle(Constants.selectedStyle);
        sentFRBtn.setStyle(Constants.btnStyle);

        friendRPane.setVisible(true);
        sentFrPane.setVisible(false);
    }

    public void showSentFrRequests() {
        sentFRBtn.setStyle(Constants.selectedStyle);
        fRequestBtn.setStyle(Constants.btnStyle);

        friendRPane.setVisible(false);
        sentFrPane.setVisible(true);
    }

    public void showAboutPane() {
        accountInfoPane.toFront();
        friendsPane.toBack();
        frequestsPane.toBack();

        aboutBtn.setStyle(Constants.selectedStyle);
        friendsBtn.setStyle(Constants.btnStyle);
        fRequestsBtn.setStyle(Constants.btnStyle);
        deleteAccountBtn.setStyle(Constants.btnStyle);
    }

    public void showRequestsPane() {
        frequestsPane.toFront();
        showFrRequests();
        friendsPane.toBack();
        accountInfoPane.toBack();

        aboutBtn.setStyle(Constants.btnStyle);
        friendsBtn.setStyle(Constants.btnStyle);
        fRequestsBtn.setStyle(Constants.selectedStyle);
        deleteAccountBtn.setStyle(Constants.btnStyle);
    }

    public void showFriendsPane() {
        friendsPane.toFront();
        frequestsPane.toBack();
        accountInfoPane.toBack();

        aboutBtn.setStyle(Constants.btnStyle);
        friendsBtn.setStyle(Constants.selectedStyle);
        fRequestsBtn.setStyle(Constants.btnStyle);
        deleteAccountBtn.setStyle(Constants.btnStyle);

        datePickerFriends.setValue("");
    }

    public void btnUpdateClicked() {
        try {
            if (userService.updateUser(loggedUser.getId(), txtUpdateFirst.getText(), txtUpdateLast.getText(), txtUpdateEmail.getText(),
                    txtUpdateUsername.getText(), txtUpdatePassword.getText(), profileImage.getImage().getUrl()) != null) {
                JOptionPane.showMessageDialog(null, "User does not exist!");
            } else {
                loggedUser = userService.getUser(loggedUser.getId());
                initAbout();
                JOptionPane.showMessageDialog(null, "User updated!");
            }
        } catch (ValidationException e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
    }

    public void btnDeleteClicked() throws IOException {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText("Attention");
        alert.setTitle("Info");
        alert.setContentText("Are you sure you want to delete your account?");

        ButtonType btnYes = new ButtonType("Yes");
        ButtonType btnNo = new ButtonType("No");
        alert.getButtonTypes().setAll(btnYes, btnNo);

        Optional<ButtonType> response = alert.showAndWait();

        if (response.get().getText().equals("Yes")) {
            userService.deleteUser(loggedUser.getId());

            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/view/login.fxml"));
            AnchorPane pane = loader.load();
            LoginController ctrl = loader.getController();
            ctrl.setService(userService, groupService, eventService, notificationService);

            Stage mainStage = new Stage();
            mainStage.initStyle(StageStyle.TRANSPARENT);
            Scene scene = new Scene(pane);
            mainStage.setScene(scene);
            mainStage.show();

            Stage stage = (Stage) deleteAccountBtn.getScene().getWindow();
            stage.close();
        }
    }

    public void ActivityBtnClicked() {
        LocalDate startDate;
        LocalDate endDate;
        if (datePicker1.getValue() == null)
            startDate = LocalDate.ofEpochDay(0L);
        else startDate = datePicker1.getValue();

        if (datePicker2.getValue() == null)
            endDate = LocalDate.now();
        else endDate = datePicker2.getValue();

        String FILE = "social-network/reports/activity.pdf";
        PdfReports pdfReports = new PdfReports(FILE);
        List<String> activities = groupService.messagesReport(startDate, endDate, loggedUser);
        activities.addAll(userService.relationsReport(startDate, endDate, loggedUser));
        if (datePicker1.getValue() == null) {
            pdfReports.addData(activities,
                    loggedUser.getFirstName() + " " + loggedUser.getLastName() + ": activity\nStartDate: -; EndDate: " + endDate + "\n");
        } else pdfReports.addData(activities,
                loggedUser.getFirstName() + " " + loggedUser.getLastName() + ": activity\nStartDate: " + startDate + "; EndDate: " + endDate + "\n");
        JOptionPane.showMessageDialog(null, "Pdf generated successfully!");
    }

    @Override
    public void update(ChangeEvent<User> event) {
        initModel();
        initAbout();
        initFR();
    }
}
