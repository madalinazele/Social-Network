package socialnetwork.controller;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import socialnetwork.domain.mainDom.User;
import socialnetwork.service.UserService;
import socialnetwork.utils.ChangeEvent;
import socialnetwork.utils.ChangeEventType;
import socialnetwork.utils.Constants;
import socialnetwork.utils.observer.Observer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SearchController implements Observer<ChangeEvent<User>> {
    UserService userService;
    User loggedUser;
    private final Map<Long, HBox> allUsersMap = new HashMap<>();

    @FXML
    private Button allUsersBtn, largestComBtn, myCommBtn;
    @FXML
    private TextField searchBar;
    @FXML
    private Label nrComLbl;
    @FXML
    private Pagination allUsersPaged, largestPaged, myCommPaged;
    @FXML
    private AnchorPane allUsersPane, myCommPane, largestPane;

    public void setService(UserService userService, User loggedUser) {
        this.userService = userService;
        this.loggedUser = loggedUser;
        userService.addObserver(this);
        userService.setPagedSize(3);
        initModel();
        allUsersBtnClicked();
    }

    @FXML
    public void initialize() {
        searchBar.textProperty().addListener((x) -> handleFilter());
    }

    private void initModel() {

        allUsersMap.clear();
        for (User user : userService.getAll()) {
            HBox hBox = new HBox();
            hBox = createProfileBox(hBox, user);
            allUsersMap.put(user.getId(), hBox);
        }
    }

    public void initAllUsers() {
        allUsersPaged.setPageCount(userService.getNumberOfPages(loggedUser.getId(), "allUsers"));
        allUsersPaged.setPageFactory(pageIdx -> {
            List<User> users = userService.getRepoUsersPage(pageIdx, loggedUser.getId(), "allUsers");
            VBox vBox = new VBox();

            for (User user : users) {
                HBox hBox = allUsersMap.get(user.getId());
                vBox.getChildren().add(hBox);
            }
            return vBox;
        });
        nrComLbl.setText("Number of communities: " + userService.getNrOfComponents());
    }

    private void showUsers(List<User> users, Pagination largestPaged) {
        largestPaged.setPageCount(userService.getNrUserPagesFiltered(users));
        largestPaged.setPageFactory(pageIdx -> {
            List<User> usersPages = userService.getUsersPageFiltered(pageIdx, users);
            VBox vBox = new VBox();

            for (User user : usersPages) {
                HBox hbox = allUsersMap.get(user.getId());
                vBox.getChildren().add(hbox);
            }
            return vBox;
        });
    }

    public void initLargestComm() {
        List<User> largestListAll = userService.getLargestComponent();
        showUsers(largestListAll, largestPaged);
        nrComLbl.setText("Number of communities: " + userService.getNrOfComponents());
    }

    public void initMyComm() {
        List<User> myCommListAll = userService.findCommunity(loggedUser.getId());
        showUsers(myCommListAll, myCommPaged);
        nrComLbl.setText("Number of communities: " + userService.getNrOfComponents());
    }

    public void handleFilter() {
        if (allUsersBtn.getStyle().equals("-fx-background-color: #27496d;")) {
            List<User> users = userService.getAll().stream()
                    .filter(x -> x.getFirstName().contains(searchBar.getText()) ||
                            x.getLastName().contains(searchBar.getText()))
                    .collect(Collectors.toList());

            showUsers(users, allUsersPaged);
        } else if (largestComBtn.getStyle().equals("-fx-background-color: #27496d;")) {

            List<User> users = userService.getLargestComponent().stream()
                    .filter(x -> x.getFirstName().contains(searchBar.getText()) ||
                            x.getLastName().contains(searchBar.getText()))
                    .collect(Collectors.toList());

            showUsers(users, largestPaged);

        } else {
            List<User> users = userService.findCommunity(loggedUser.getId()).stream()
                    .filter(x -> x.getFirstName().contains(searchBar.getText()) ||
                            x.getLastName().contains(searchBar.getText()))
                    .collect(Collectors.toList());

            showUsers(users, myCommPaged);
        }
    }

    public HBox createProfileBox(HBox hbox, User user) {

        hbox.getChildren().clear();

        hbox.setStyle("-fx-background-color: #000328; -fx-border-width: 10; -fx-border-color: #000328;");
        hbox.setMinSize(455, 80);
        hbox.setMaxSize(455, 80);
        Image pic = new Image(user.getImage(), 60, 60, false, false);
        ImageView profilePic = new ImageView(pic);
        hbox.getChildren().add(profilePic);

        VBox vBox = new VBox();

        Label nume = new Label(user.getFirstName() + " " + user.getLastName());
        nume.setStyle("-fx-font-family: Segoe UI; -fx-font-weight: bold; -fx-font-size: 14; -fx-text-alignment: center; -fx-text-fill: #ffffff;");
        Label username = new Label(user.getUsername());
        username.setStyle("-fx-font-family: Segoe UI; -fx-font-weight: bold; -fx-font-size: 12; -fx-text-fill: #ffffff;");
        Label email = new Label(user.getEmail());
        email.setStyle("-fx-font-family: Segoe UI; -fx-font-style: italic ; -fx-font-size: 12;-fx-text-fill: #ffffff;");
        vBox.getChildren().addAll(nume, username, email);

        hbox.getChildren().add(vBox);

        if (!user.getId().equals(loggedUser.getId())) {
            if (userService.findFriendship(loggedUser.getId(), user.getId()) == null) {
                if (userService.findFriendRequest(loggedUser.getId(), user.getId()) != null) {
                    Button button = new Button("Cancel friend request");
                    button.setStyle("-fx-background-color: #27496d; -fx-text-fill: #ffffff;");
                    button.setOnAction(event -> {
                        userService.cancelFriendRequest(loggedUser.getId(), user.getId());
                        createProfileBox(allUsersMap.get(user.getId()), user);
                    });
                    hbox.getChildren().add(button);
                    HBox.setHgrow(button, Priority.ALWAYS);
                } else if (userService.findFriendRequest(user.getId(), loggedUser.getId()) != null) {
                    Button button = new Button("Accept");
                    button.setStyle("-fx-background-color: #27496d; -fx-text-fill: #ffffff;");
                    button.setOnAction(event -> {
                        userService.respondFriendRequest(loggedUser.getId(), user.getId(), "yes");
                        createProfileBox(allUsersMap.get(user.getId()), user);
                    });
                    hbox.getChildren().add(button);
                    HBox.setHgrow(button, Priority.ALWAYS);

                    button = new Button("Decline");
                    button.setStyle("-fx-background-color: #27496d; -fx-text-fill: #ffffff;");
                    button.setOnAction(event -> {
                        userService.respondFriendRequest(loggedUser.getId(), user.getId(), "no");
                        createProfileBox(allUsersMap.get(user.getId()), user);
                    });
                    hbox.getChildren().add(button);
                    HBox.setHgrow(button, Priority.ALWAYS);
                } else {
                    Button button = new Button("Send friend request");
                    button.setStyle("-fx-background-color: #27496d; -fx-text-fill: #ffffff;");
                    button.setOnAction(event -> {
                        userService.sendFriendRequest(loggedUser.getId(), user.getId());
                        createProfileBox(allUsersMap.get(user.getId()), user);
                    });
                    hbox.getChildren().add(button);
                    HBox.setHgrow(button, Priority.ALWAYS);
                }
            }
        }
        hbox.setSpacing(20);
        HBox.setHgrow(vBox, Priority.ALWAYS);
        hbox.setAlignment(Pos.BOTTOM_RIGHT);

        return hbox;
    }

    public void allUsersBtnClicked() {
        allUsersBtn.setStyle(Constants.selectedStyle);
        largestComBtn.setStyle(Constants.btnStyle);
        myCommBtn.setStyle(Constants.btnStyle);
        searchBar.clear();

        initAllUsers();
        myCommPane.setVisible(false);
        allUsersPane.setVisible(true);
        largestPane.setVisible(false);
        allUsersPaged.setCurrentPageIndex(0);
    }

    public void largestCommClicked() {
        largestComBtn.setStyle(Constants.selectedStyle);
        allUsersBtn.setStyle(Constants.btnStyle);
        myCommBtn.setStyle(Constants.btnStyle);
        searchBar.clear();

        initLargestComm();
        myCommPane.setVisible(false);
        allUsersPane.setVisible(false);
        largestPane.setVisible(true);
        largestPaged.setCurrentPageIndex(0);
    }

    public void myCommClicked() {
        myCommBtn.setStyle(Constants.selectedStyle);
        allUsersBtn.setStyle(Constants.btnStyle);
        largestComBtn.setStyle(Constants.btnStyle);
        searchBar.clear();

        initMyComm();
        myCommPane.setVisible(true);
        allUsersPane.setVisible(false);
        largestPane.setVisible(false);
        myCommPaged.setCurrentPageIndex(0);
    }

    @Override
    public void update(ChangeEvent<User> event) {
        createProfileBox(allUsersMap.get(event.getData().getId()), event.getData());

        if (event.getType() != ChangeEventType.FRIENDREQUEST) {
            initLargestComm();
            initMyComm();
        }
    }
}
