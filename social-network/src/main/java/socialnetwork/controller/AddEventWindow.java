package socialnetwork.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import socialnetwork.domain.mainDom.User;
import socialnetwork.service.EventService;

import javax.swing.*;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class AddEventWindow {
    EventService eventService;
    User loggedUser;

    @FXML
    private TextField titleTxt, descriptionTxt;

    @FXML
    private DatePicker datePicker;

    @FXML
    private Button createEventBtn;

    @FXML
    private ChoiceBox<Integer> hourPicker, minutesPicker;

    public void setService(EventService eventService, User loggedUser) {
        this.eventService = eventService;
        this.loggedUser = loggedUser;
    }

    public void initialize() {
        for (int i = 0; i < 24; ++i) hourPicker.getItems().add(i);
        for (int i = 0; i < 60; ++i) minutesPicker.getItems().add(i);
    }

    public void createEventClicked() {
        if (titleTxt.getText().equals(""))
            JOptionPane.showMessageDialog(null, "Please enter a title");
        else if (datePicker.getValue() == null)
            JOptionPane.showMessageDialog(null, "Please select a date!");
        else {
            String title = titleTxt.getText();
            LocalDateTime date = LocalDateTime.of(datePicker.getValue(), LocalTime.of(hourPicker.getValue(), minutesPicker.getValue()));
            String description = descriptionTxt.getText();

            eventService.addEvent(title, description, date, loggedUser);
            JOptionPane.showMessageDialog(null, "Event added");

            Stage st = (Stage) createEventBtn.getScene().getWindow();
            st.close();
        }
    }

    public void exitIconClicked() {
        Stage st = (Stage) createEventBtn.getScene().getWindow();
        st.close();
    }
}
