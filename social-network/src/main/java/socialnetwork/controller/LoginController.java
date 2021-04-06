package socialnetwork.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import socialnetwork.domain.mainDom.User;
import socialnetwork.domain.validators.ValidationException;
import socialnetwork.service.EventService;
import socialnetwork.service.GroupService;
import socialnetwork.service.NotificationService;
import socialnetwork.service.UserService;
import socialnetwork.utils.Constants;

import javax.swing.*;
import java.io.File;
import java.io.IOException;

public class LoginController {

    UserService userService;
    GroupService groupService;
    EventService eventService;
    NotificationService notificationService;

    @FXML
    private AnchorPane loginPane, signUpPane;
    @FXML
    private Button loginBtn, signUpBtn, loginBtn2;
    @FXML
    private TextField usernameIn, emailIn, firstName, lastName, usernameUp, emailUp;
    @FXML
    private PasswordField passwordIn, passwordUp;
    @FXML
    private ImageView profileImg;

    public void setService(UserService userService, GroupService groupService, EventService eventService, NotificationService notificationService) {
        this.userService = userService;
        this.groupService = groupService;
        this.eventService = eventService;
        this.notificationService = notificationService;
    }

    public void showLoginPane() {
        signUpPane.setVisible(false);
        loginPane.setVisible(true);
        loginBtn.setStyle(Constants.selectedStyle);
        signUpBtn.setStyle(Constants.btnStyle);
    }

    public void showSignUpPane() {
        loginPane.setVisible(false);
        signUpPane.setVisible(true);
        signUpBtn.setStyle(Constants.selectedStyle);
        loginBtn.setStyle(Constants.btnStyle);
    }

    public void signUpClicked() {
        try {
            try {
                userService.addUser(firstName.getText(), lastName.getText(), emailUp.getText(), usernameUp.getText(), passwordUp.getText(), profileImg.getImage().getUrl());
                JOptionPane.showMessageDialog(null, "Account created successfully! ");
            } catch (ValidationException e) {
                JOptionPane.showMessageDialog(null, e.getMessage());
            }
        } catch (ValidationException e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
    }

    public void loginClicked() throws IOException {
        User user = validateLogin(usernameIn.getText(), passwordIn.getText(), emailIn.getText());
        if (user == null)
            JOptionPane.showMessageDialog(null, "Invalid user data!");
        else {
            JOptionPane.showMessageDialog(null, "Login successful!");

            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/view/home.fxml"));
            AnchorPane root = loader.load();
            HomeController ctrl = loader.getController();
            ctrl.setService(userService, groupService, eventService, notificationService, user);

            Stage mainStage = new Stage();
            mainStage.initStyle(StageStyle.TRANSPARENT);
            Scene scene = new Scene(root);
            mainStage.setScene(scene);
            mainStage.show();

            Stage st = (Stage) loginBtn2.getScene().getWindow();
            st.close();
        }
    }

    public void chooseImgClicked() {
        Stage st = (Stage) signUpBtn.getScene().getWindow();
        FileChooser fc = new FileChooser();
        fc.setInitialDirectory(new File("C:/Users/Madalina/Desktop/social-network/src/main/resources/view/images/profile"));
        File selectedFile = fc.showOpenDialog(st);
        if (selectedFile != null) {
            Image img = new Image(selectedFile.toURI().toString(), 60, 60, false, false);
            profileImg.setImage(img);
        }
    }

    public User validateLogin(String username, String password, String email) {
        for (User user : userService.getAll()) {
            if (user.getUsername().equals(username) && user.getPassword().equals(password) && user.getEmail().equals(email))
                return user;
        }
        return null;
    }

    public void exitBtnClicked() {
        Stage st = (Stage) loginBtn2.getScene().getWindow();
        st.close();
    }
}
