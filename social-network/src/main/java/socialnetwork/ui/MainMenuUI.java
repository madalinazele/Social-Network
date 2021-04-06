package socialnetwork.ui;

import socialnetwork.domain.mainDom.User;
import socialnetwork.domain.validators.ValidationException;
import socialnetwork.service.GroupService;
import socialnetwork.service.UserService;
import socialnetwork.ui.menu.MenuUI;

import java.util.Scanner;

public class MainMenuUI extends UIMain {

    private UserService userService;
    private GroupService groupService;
    private MenuUI menuUI;

    public MainMenuUI(UserService userService, GroupService groupService) {
        this.userService = userService;
        this.groupService = groupService;
        menuUI = new MenuUI(userService, groupService);
    }

    private void menu() {
        System.out.println("----------");
        System.out.println("1. Login");
        System.out.println("2. Register");
        System.out.println("0. Exit");
        System.out.println("----------");
    }

    private String input(String msg) {
        Scanner scanner = new Scanner(System.in);
        System.out.print(msg);
        return scanner.nextLine();
    }

    public void run() {
        int idx = -1;

        while (idx != 0) {

            try {
                menu();
                idx = Integer.parseInt(input("Enter option: "));

                switch (idx) {
                    case 1:
                        collectLoginData();
                        break;
                    case 2:
                        register();
                        break;
                    case 0:
                        break;
                    default:
                        System.out.println("Invalid option!");
                }


            } catch (NumberFormatException e) {
                System.out.println("Invalid option");
            }
        }
    }

    private void collectLoginData() {
        String username = input("Enter username: ");
        String password = input("Enter password: ");
        String email = input("Enter email: ");

        User current = validateLogin(username, password, email);
        if (current == null)
            System.out.println("Invalid login data!");
        else {
            System.out.println("Login successful!");
            currentUser = current;
            menuUI.run();
        }
    }

    private void register() {
        String firstName = input("Enter first name: ");
        String lastName = input("Enter last name: ");
        String email = input("Enter email: ");
        String username = input("Enter username: ");
        String password = input("Enter password: ");
        String image = input("Enter image URL: ");

        try {
            userService.addUser(firstName, lastName, email, username, password, image);
            System.out.println("User created successfully!");

        } catch (ValidationException e) {
            System.out.println(e.getMessage());
        }
    }

    public User validateLogin(String username, String password, String email) {
        for (User user : userService.getAll()) {
            if (user.getUsername().equals(username) && user.getPassword().equals(password) && user.getEmail().equals(email))
                return user;
        }
        return null;
    }
}
