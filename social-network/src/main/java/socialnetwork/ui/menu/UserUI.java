package socialnetwork.ui.menu;

import socialnetwork.domain.mainDom.User;
import socialnetwork.domain.validators.ValidationException;
import socialnetwork.service.UserService;
import socialnetwork.ui.UIMain;

import java.util.List;
import java.util.Scanner;

public class UserUI extends UIMain {

    private UserService userService;

    public UserUI(UserService userService) {
        this.userService = userService;
    }

    private String input(String msg) {
        Scanner scanner = new Scanner(System.in);
        System.out.print(msg);
        return scanner.nextLine();
    }

    public void menu() {
        System.out.println("\n----------");
        //System.out.println("1. Add user");
        System.out.println("1. Update account");
        System.out.println("2. Delete account");
        System.out.println("3. Display all users");
        System.out.println("4. Find user");
        System.out.println("0. Back");
        System.out.println("----------\n");
    }

    public void run() {
        int idx = -1;

        while (idx != 0) {

            try {
                menu();
                idx = Integer.parseInt(input("Enter option: "));
                switch (idx) {
                    case 0:
                        break;
                    case 1:
                        this.updateUser();
                        break;
                    case 2:
                        this.deleteUser();
                        if (currentUser == null)
                            return;
                        break;
                    case 3:
                        this.displayAll();
                        break;
                    case 4:
                        this.findUserByUsername();
                        break;
                    default:
                        System.out.println("Invalid option!");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid option!");
            }
        }
    }

    public void deleteUser() {
        try {
            Long id = currentUser.getRole().equalsIgnoreCase("admin") ? Long.parseLong(input("Enter id: ")) : currentUser.getId();

            User deletedUser = userService.deleteUser(id);
            if (deletedUser == null) {
                System.out.println("User does not exist!");
            } else {
                System.out.println("User deleted!");
                if (currentUser.getId().equals(deletedUser.getId()))
                    currentUser = null;
            }
        } catch (NumberFormatException e) {
            System.out.println("id must be a number");
        }
    }

    public void updateUser() {
        try {
            Long id = currentUser.getRole().equalsIgnoreCase("admin") ? Long.parseLong(input("Enter id: ")) : currentUser.getId();
            String firstName = input("Enter first name: ");
            String lastName = input("Enter last name: ");
            String email = input("Enter email: ");
            String username = input("Enter username: ");
            String password = input("Enter password: ");
            String image = input("Enter imageURL: ");

            try {
                if (userService.updateUser(id, firstName, lastName, email, username, password, image) != null) {
                    System.out.println("User does not exist!");
                } else System.out.println("User updated!");
            } catch (ValidationException e) {
                System.out.println(e.getMessage());
            }
        } catch (NumberFormatException e) {
            System.out.println("id must be a number");
        }
    }

    public void displayAll() {
        List<User> users = userService.getAll();
        users.forEach(System.out::println);
    }

    public void findUserByUsername() {
        String username = input("Enter username: ");
        User user = userService.getUserByUsername(username);
        if (user == null)
            System.out.println("There is no user with the specified username!");
        else System.out.println(user);
    }
}
