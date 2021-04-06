package socialnetwork.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import socialnetwork.domain.dto.FriendsDTO;
import socialnetwork.domain.mainDom.Group;
import socialnetwork.domain.mainDom.User;
import socialnetwork.service.GroupService;
import socialnetwork.service.UserService;
import socialnetwork.utils.ChangeEvent;
import socialnetwork.utils.Constants;
import socialnetwork.utils.PdfReports;
import socialnetwork.utils.observer.Observer;

import javax.swing.*;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public class GroupsController implements Observer<ChangeEvent<User>> {

    UserService userService;
    GroupService groupService;
    User loggedUser;

    @FXML
    private Button groupsBtn, privateChatsBtn;
    @FXML
    private Pagination groupsPaged, privateChatsPaged;
    @FXML
    private ComboBox<String> friendPicker;
    @FXML
    private DatePicker datePicker1, datePicker2;

    public void setService(UserService userService, GroupService groupService, User loggedUser) {
        this.userService = userService;
        this.groupService = groupService;
        this.loggedUser = loggedUser;
        groupService.addObserver(this);
        userService.addObserver(this);
        groupService.setPageSizeGroup(4);
        initFriends();
        showGroupsPane();
        //initModel();
    }

    public void initFriends() {
        friendPicker.getItems().clear();
        for (FriendsDTO u : userService.findFriends(loggedUser.getId())) {
            friendPicker.getItems().add(u.getUser().getUsername());
        }
        friendPicker.getItems().add("");
        friendPicker.setValue("");
    }

    public void initModel() {
        initGroups();
        initPrivateChats();
        initFriends();
    }

    public void initGroups() {
        groupsPaged.setPageCount(groupService.getNrGroupsPages(loggedUser.getId(), "groups"));
        groupsPaged.setPageFactory(pageIdx -> {
            VBox groupVBox = new VBox();
            List<Group> groups = groupService.getGroupsOnPage(pageIdx, loggedUser.getId(), "groups");

            for (Group group : groups) {
                Button button = createGroupBox(group);
                groupVBox.getChildren().add(button);
            }
            return groupVBox;
        });
    }

    public Button createGroupBox(Group group) {
        Button btn = new Button();
        btn.setStyle(" -fx-background-color: #000328; -fx-font-family: Segoe UI; -fx-font-weight: bold; -fx-font-size: 20; -fx-text-alignment: center; -fx-text-fill: #ffffff;");
        btn.setMinHeight(50);
        btn.setPrefSize(400, 50);
        btn.setText(group.getGroupName());
        btn.setOnAction(this::openGroup);

        Label idLbl = new Label(String.valueOf(group.getId()));
        btn.setGraphic(idLbl);
        idLbl.setVisible(false);

        return btn;
    }

    public void initPrivateChats() {
        privateChatsPaged.setPageCount(groupService.getNrGroupsPages(loggedUser.getId(), "privateChats"));
        privateChatsPaged.setPageFactory(pageIdx -> {
            List<Group> privateChats = groupService.getGroupsOnPage(pageIdx, loggedUser.getId(), "privateChats");
            VBox vBox = new VBox();

            for (Group chat : privateChats) {
                String userName;
                if (!chat.getMembers().get(0).equals(loggedUser.getId())) {
                    userName = userService.getUser(chat.getMembers().get(0)).getUsername();
                } else userName = userService.getUser(chat.getMembers().get(1)).getUsername();

                Button btn = new Button();
                btn.setStyle(" -fx-background-color: #000328; -fx-font-family: Segoe UI; -fx-font-weight: bold; -fx-font-size: 20; -fx-text-alignment: center; -fx-text-fill: #ffffff;");
                btn.setMinHeight(50);
                btn.setPrefSize(400, 50);
                btn.setText(userName);
                btn.setOnAction(this::openPrivateChat);
                vBox.getChildren().add(btn);
            }
            return vBox;
        });
    }

    public void showGroupsPane() {
        groupsBtn.setStyle(Constants.selectedStyle);
        privateChatsBtn.setStyle(Constants.btnStyle);

        initGroups();
        groupsPaged.setVisible(true);
        privateChatsPaged.setVisible(false);
    }

    public void showPrivateChatsPane() {
        privateChatsBtn.setStyle(Constants.selectedStyle);
        groupsBtn.setStyle(Constants.btnStyle);

        initPrivateChats();
        privateChatsPaged.setVisible(true);
        groupsPaged.setVisible(false);
    }

    public void createGroupClicked() throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/view/addGroupForm.fxml"));
        AnchorPane pane = loader.load();
        AddGroupWindow ctrl = loader.getController();
        ctrl.setService(groupService, loggedUser);

        Stage mainStage = new Stage();
        mainStage.initStyle(StageStyle.TRANSPARENT);
        Scene scene = new Scene(pane);
        mainStage.setScene(scene);
        mainStage.show();
    }

    public void openGroup(ActionEvent ev) {
        Button groupBtn = (Button) ev.getSource();
        Label idLbl = (Label) groupBtn.getGraphic();

        Group group = groupService.findGroupById(Long.parseLong(idLbl.getText()));

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/view/openChat.fxml"));
        try {
            SplitPane pane = loader.load();
            MessageController ctrl = loader.getController();
            ctrl.setService(groupService, loggedUser, group);

            Stage mainStage = new Stage();
            mainStage.initStyle(StageStyle.TRANSPARENT);
            Scene scene = new Scene(pane);
            mainStage.setScene(scene);
            mainStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void openPrivateChat(ActionEvent ev) {
        Button btn = (Button) ev.getSource();

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/view/openChat.fxml"));
        try {
            SplitPane pane = loader.load();
            Group pC = groupService.openPrivateChat(loggedUser.getId(), userService.getUserByUsername(btn.getText()).getId());
            MessageController ctrl = loader.getController();
            ctrl.setService(groupService, loggedUser, pC);

            Stage mainStage = new Stage();
            mainStage.initStyle(StageStyle.TRANSPARENT);
            Scene scene = new Scene(pane);
            mainStage.setScene(scene);
            mainStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void conversationBtn() {
        if (!friendPicker.getValue().equals("")) {
            LocalDate startDate, endDate;
            if (datePicker1.getValue() == null)
                startDate = LocalDate.of(1970, 1, 1);
            else startDate = datePicker1.getValue();
            if (datePicker2.getValue() == null)
                endDate = LocalDate.now();
            else endDate = datePicker2.getValue();

            String FILE = "social-network/reports/conversations.pdf";
            PdfReports pdfReports = new PdfReports(FILE);
            if (datePicker1.getValue() == null)
                pdfReports.addData(groupService.conversationsReport(startDate, endDate, loggedUser, userService.getUserByUsername(friendPicker.getValue())),
                        loggedUser.getFirstName() + " " + loggedUser.getLastName() + ": messages\nStartDate: -; EndDate: " + endDate + "\n");
            pdfReports.addData(groupService.conversationsReport(startDate, endDate, loggedUser, userService.getUserByUsername(friendPicker.getValue())),
                    loggedUser.getFirstName() + " " + loggedUser.getLastName() + ": messages\nStartDate: " + startDate + "; EndDate: " + endDate + "\n");

            JOptionPane.showMessageDialog(null, "Pdf generated successfully!");
        } else {
            JOptionPane.showMessageDialog(null, "No friend selected!");
        }
    }

    @Override
    public void update(ChangeEvent<User> event) {
        initModel();
    }
}
