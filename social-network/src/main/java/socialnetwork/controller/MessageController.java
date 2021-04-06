package socialnetwork.controller;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import socialnetwork.domain.mainDom.Group;
import socialnetwork.domain.mainDom.Message;
import socialnetwork.domain.mainDom.User;
import socialnetwork.service.GroupService;
import socialnetwork.utils.ChangeEvent;
import socialnetwork.utils.Constants;
import socialnetwork.utils.observer.Observer;

import javax.swing.*;
import java.util.Comparator;
import java.util.List;

public class MessageController implements Observer<ChangeEvent<User>> {

    GroupService groupService;
    User loggedUser;
    Group currentGroup;

    @FXML
    private VBox membersVBox, messagesVBox;
    @FXML
    private TextField textMessageTxt;
    @FXML
    private Button sendMessageBtn;

    public void setService(GroupService groupService, User user, Group group) {
        this.groupService = groupService;
        this.loggedUser = user;
        this.currentGroup = group;
        groupService.addObserver(this);
        sendMessageBtn.setText("Send message");
        initModel();
        initMembers();
    }

    public void initModel() {
        messagesVBox.getChildren().clear();
        groupService.setPageSizeMsg(groupService.getAllMessagesFromGroup(currentGroup).size());

        int page = 0;
        List<Message> messages = groupService.getMessagesOnPage(page, currentGroup.getId(), "all");
        messages.sort(Comparator.comparing(Message::getDate));
        for (Message msg : messages) {
            if (msg.getReply() != null) {
                VBox replyBox = createReplyMessageBox(groupService.findMessageInGroup(msg.getReply(), currentGroup));
                messagesVBox.getChildren().add(replyBox);
            }
            VBox vBox = createMessageBox(msg);
            messagesVBox.getChildren().add(vBox);
        }
    }

    public VBox createMessageBox(Message message) {
        VBox vBox = new VBox();
        vBox.setStyle("-fx-background-color: #000328");
        vBox.setMinSize(450, 50);
        vBox.setMaxSize(450, 50);

        Label text = new Label(message.getMessage());
        text.setStyle("-fx-font-family: Segoe UI; -fx-font-weight: bold; -fx-font-size: 16; -fx-text-alignment: center; -fx-text-fill: #ffffff;");
        vBox.getChildren().add(text);

        HBox hbox = new HBox();
        hbox.setStyle("-fx-background-color: #000328; /*-fx-border-width: 10; -fx-border-color: #000328;*/");

        Label from = new Label("    " + message.getFrom().getUsername());
        from.setStyle("-fx-font-family: Segoe UI; -fx-font-weight: bold; -fx-font-size: 14; -fx-text-fill: #ffffff;");
        Label date = new Label(message.getDate().format(Constants.DATE_TIME_FORMATTER));
        date.setStyle("-fx-font-family: Segoe UI; -fx-font-style: italic ; -fx-font-size: 14;-fx-text-fill: #ffffff;");

        Button button = new Button("Reply");
        button.setStyle("-fx-background-color: #27496d; -fx-text-fill: #ffffff;");
        button.setOnAction((x) -> replyMessageClicked(message));

        hbox.getChildren().addAll(from, date, button);
        HBox.setHgrow(from, Priority.ALWAYS);
        HBox.setHgrow(date, Priority.ALWAYS);
        HBox.setHgrow(button, Priority.ALWAYS);
        hbox.setAlignment(Pos.TOP_LEFT);
        hbox.setSpacing(20);

        vBox.getChildren().add(hbox);
        VBox.setVgrow(text, Priority.ALWAYS);
        VBox.setVgrow(hbox, Priority.ALWAYS);
        return vBox;
    }

    public VBox createReplyMessageBox(Message message) {
        VBox vBox = new VBox();
        vBox.setStyle("-fx-background-color: #000328;");
        vBox.setMinSize(400, 40);
        vBox.setMaxSize(400, 40);

        Label reply = new Label("   Replied to: ");
        reply.setStyle("-fx-font-family: Segoe UI; -fx-font-style: italic; -fx-font-size: 14; -fx-text-fill: #ffffff;");
        Label text = new Label(message.getMessage());
        text.setStyle("-fx-font-family: Segoe UI; -fx-font-weight: bold; -fx-font-size: 14; -fx-text-alignment: center; -fx-text-fill: #ffffff;");
        HBox msgHBox = new HBox();
        msgHBox.getChildren().addAll(reply, text);

        vBox.getChildren().add(msgHBox);

        HBox hbox = new HBox();
        hbox.setStyle("-fx-background-color: #000328;");

        Label from = new Label("    " + message.getFrom().getUsername());
        from.setStyle("-fx-font-family: Segoe UI; -fx-font-weight: bold; -fx-font-size: 12; -fx-text-fill: #ffffff;");
        Label date = new Label(message.getDate().format(Constants.DATE_TIME_FORMATTER));
        date.setStyle("-fx-font-family: Segoe UI; -fx-font-style: italic ; -fx-font-size: 12;-fx-text-fill: #ffffff;");

        hbox.getChildren().addAll(from, date);
        HBox.setHgrow(from, Priority.ALWAYS);
        HBox.setHgrow(date, Priority.ALWAYS);
        hbox.setAlignment(Pos.CENTER_LEFT);
        hbox.setSpacing(15);

        vBox.getChildren().add(hbox);
        VBox.setVgrow(msgHBox, Priority.ALWAYS);
        VBox.setVgrow(hbox, Priority.ALWAYS);
        return vBox;
    }

    public void initMembers() {
        membersVBox.getChildren().clear();
        groupService.getAllMembersFromGroup(currentGroup)
                .forEach(x -> {
                    Button btn = new Button();
                    btn.setStyle(" -fx-background-color: #000328; -fx-font-family: Segoe UI; -fx-font-weight: bold; -fx-font-size: 14; -fx-text-alignment: center; -fx-text-fill: #ffffff;");
                    btn.setPrefSize(150, 40);
                    btn.setText(x.getUsername());
                    membersVBox.getChildren().add(btn);
                });
    }

    public void sendMessageClicked() {
        if (!textMessageTxt.getText().equals("")) {
            groupService.sendMessageToGroup(loggedUser, currentGroup, textMessageTxt.getText());
            textMessageTxt.clear();
        } else {
            JOptionPane.showMessageDialog(null, "You can't send an empty message!");
        }
    }

    public void replyMessageClicked(Message message) {
        if (message == null) {
            JOptionPane.showMessageDialog(null, "No message selected!");
        } else if (textMessageTxt.getText().equals("")) {
            JOptionPane.showMessageDialog(null, "You can't send an empty message!");
        } else {
            groupService.replyMessage(loggedUser, message.getId(), currentGroup, textMessageTxt.getText());
            textMessageTxt.clear();
            initModel();
        }
    }

    public void exitIconClicked() {
        Stage st = (Stage) sendMessageBtn.getScene().getWindow();
        st.close();
    }

    @Override
    public void update(ChangeEvent<User> event) {
        initModel();
    }
}
