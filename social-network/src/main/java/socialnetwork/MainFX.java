package socialnetwork;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Pair;
import socialnetwork.config.ApplicationContext;
import socialnetwork.controller.LoginController;
import socialnetwork.domain.Tuple;
import socialnetwork.domain.dto.MessageDTO;
import socialnetwork.domain.dto.NotificationDTO;
import socialnetwork.domain.mainDom.*;
import socialnetwork.domain.validators.MessageValidator;
import socialnetwork.domain.validators.RelationValidator;
import socialnetwork.domain.validators.UserValidator;
import socialnetwork.repository.database.*;
import socialnetwork.repository.paging.PagingRepository;
import socialnetwork.service.EventService;
import socialnetwork.service.GroupService;
import socialnetwork.service.NotificationService;
import socialnetwork.service.UserService;

public class MainFX extends Application {

    UserService userService;
    GroupService groupService;
    EventService eventService;
    NotificationService notificationService;

    @Override
    public void start(Stage primaryStage) throws Exception {
        final String url = ApplicationContext.getPROPERTIES().getProperty("database.socialnetwork.url");
        final String username = ApplicationContext.getPROPERTIES().getProperty("database.socialnetwork.username");
        final String pasword = ApplicationContext.getPROPERTIES().getProperty("database.socialnetwork.pasword");
        PagingRepository<Long, User> userDBRepository =
                new UserDBRepository(url, username, pasword, new UserValidator());
        PagingRepository<Tuple<Long, Long>, Relation> relationDBRepository =
                new RelationDBRepository(url, username, pasword, new RelationValidator());
        PagingRepository<Long, MessageDTO> messageDTODBRepository =
                new MessageDBRepository(url, username, pasword, new MessageValidator());
        PagingRepository<Long, Group> groupDBRepository =
                new GroupDBRepository(url, username, pasword);
        PagingRepository<Tuple<Long, Long>, FriendRequest> friendRequestRepository =
                new FrRequestDBRepository(url, username, pasword);
        MembersRepository<Long, Event, Pair<Long, Boolean>> eventDBRepository =
                new EventDBRepository(url, username, pasword);
        PagingRepository<Long, NotificationDTO> notificationRepository =
                new NotificationDBRepository(url, username, pasword);

        notificationService = new NotificationService(notificationRepository, userDBRepository);
        userService = new UserService(userDBRepository, relationDBRepository, friendRequestRepository, notificationService);
        groupService = new GroupService(groupDBRepository, messageDTODBRepository, userDBRepository, notificationService);
        eventService = new EventService(eventDBRepository, userDBRepository, notificationService);

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/view/login.fxml"));
        AnchorPane root = loader.load();

        LoginController ctrl = loader.getController();
        ctrl.setService(userService, groupService, eventService, notificationService);

        primaryStage.initStyle(StageStyle.TRANSPARENT);
        primaryStage.setScene(new Scene(root));
        primaryStage.setTitle("Social Network");
        primaryStage.show();

    }

    public static void main(String[] args) {
        launch(args);
    }
}
