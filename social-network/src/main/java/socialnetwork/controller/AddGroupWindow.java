package socialnetwork.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import socialnetwork.domain.mainDom.User;
import socialnetwork.service.GroupService;
import socialnetwork.service.ServiceException;

import javax.swing.*;
import java.util.List;
import java.util.stream.Collectors;

public class AddGroupWindow {

    GroupService groupService;
    User loggedUser;

    @FXML
    private VBox textVBox;
    @FXML
    private TextField groupNameTxt;
    @FXML
    private Button createGroupBtn;

    public void setService(GroupService groupService, User loggedUser) {
        this.groupService = groupService;
        this.loggedUser = loggedUser;
    }

    public void addIconClicked() {
        TextField txt = new TextField();
        txt.setMinHeight(40);
        txt.setPromptText("Enter username");
        txt.setId("groupNameTxt");
        textVBox.getChildren().add(txt);
    }

    public void createGroupClicked() {
        if (groupNameTxt.getText().equals("")) {
            JOptionPane.showMessageDialog(null, "Please enter the group name");
        } else {
            List<TextField> textsList = textVBox.getChildren().stream()
                    .map(x -> (TextField) x)
                    .collect(Collectors.toList());
            List<String> users = textsList.stream()
                    .filter(x -> !x.getText().equals("") && !x.getId().equals("groupNameTxt"))
                    .map(TextInputControl::getText).collect(Collectors.toList());

            if (users.size() < 2) {
                JOptionPane.showMessageDialog(null, "A group must lave at least 2 friends!");
            } else {
                try {
                    users.add(loggedUser.getUsername());
                    groupService.createGroupWithUsernames(groupNameTxt.getText(), users);
                    JOptionPane.showMessageDialog(null, "Group created");

                    Stage st = (Stage) createGroupBtn.getScene().getWindow();
                    st.close();
                } catch (ServiceException e) {
                    JOptionPane.showMessageDialog(null, e.getMessage());
                }
            }
        }
    }

    public void createPrivateChatClicked() {
        List<TextField> textsList = textVBox.getChildren().stream()
                .map(x -> (TextField) x)
                .collect(Collectors.toList());
        List<String> users = textsList.stream()
                .filter(x -> !x.getText().equals("") && !x.getId().equals("groupNameTxt"))
                .map(TextInputControl::getText).collect(Collectors.toList());

        if (users.size() > 1) {
            JOptionPane.showMessageDialog(null, "Too many users! Try creating a group instead!");
        } else {
            try {
                users.add(loggedUser.getUsername());
                groupService.createGroupWithUsernames("private chat", users);
                JOptionPane.showMessageDialog(null, "Chat created");

                Stage st = (Stage) createGroupBtn.getScene().getWindow();
                st.close();
            } catch (ServiceException e) {
                JOptionPane.showMessageDialog(null, e.getMessage());
            }
        }
    }

    public void exitIconClicked() {
        Stage st = (Stage) createGroupBtn.getScene().getWindow();
        st.close();
    }
}
